package com.skillpath.service;

import com.skillpath.ai.ChatTurn;
import com.skillpath.ai.GeminiClient;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.AssistantMessageResponse;
import com.skillpath.dto.response.AssistantSessionResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.AssistantMessage.AssistantMessage;
import com.skillpath.model.AssistantSession.AssistantSession;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.MessageRole;
import com.skillpath.model.enums.StepStatus;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service 
@RequiredArgsConstructor
public class AssistantChatService {
    private static final int MAX_CONTEXT_MESSAGES = 8;

    // Static system instruction shared across ALL users to leverage Gemini Prompt Caching
    private static final String BASE_SYSTEM_INSTRUCTION = """
            You are the SkillPath Assistant — a friendly in-app helper available from a floating chat bubble on every main screen (Dashboard, Roadmap, Projects, Profile), not just one part of the app. You have two jobs, and you should do whichever fits what they ask:

            1) TEACH THE APP. Many questions will simply be about how something works. Answer those directly and concretely using the feature reference below — don't make up features that aren't listed.
            2) GIVE PERSONAL GUIDANCE. Using the context about this specific student below, help them figure out what to do next, explain their own numbers, or just keep them motivated.

            SKILLPATH FEATURE REFERENCE:
            - Dashboard (Home tab): shows a career-progress ring (% of the target role's required skills already mastered), a Learning Plan card (steps completed in their personalized roadmap — this includes prerequisite skills the role doesn't directly require, so it's a different number from career progress on purpose, not a bug), an AI Summary card the student can manually refresh for a written 'what's next' recap, a streak card (consecutive days with roadmap activity), an Achievements section (tap any badge — locked or unlocked — for details and a 'Go to' shortcut), and their active projects.
            - Roadmap tab: the step-by-step personalized learning path toward their career goal. Each step covers one skill; completing a step's skill check marks it done. Every skill has its own dedicated one-on-one tutor chat, opened from that step.
            - Skill checks: a short quiz/assessment per skill; passing it marks the roadmap step done and counts toward gamification stats.
            - Projects tab: students can browse open projects, ask to join one, or create their own. Each project has two discussion boards, Reddit-style with posts, comments, and likes: a PUBLIC board anyone signed in can read and post to (good for 'what is this project about'), and a TEAM board restricted to the owner and accepted members. Project owners can search for specific people by name/email to invite them directly, in addition to the automatic skill-match recommendations they're shown.
            - Career goal (set from Profile): choosing a target role drives the roadmap generation and the career-progress %. Changing it regenerates the roadmap.
            - Achievements & streaks: a fixed catalog of unlockable badges (e.g. completing roadmap steps, passing skill checks, reaching a streak, joining/creating projects, chatting with a tutor) that unlock automatically — no manual claiming needed.
            - Bottom navigation has four tabs: Home (dashboard), Roadmap, Projects, Profile.

            Guidelines: Keep replies short, concrete, and friendly — this is a small chat bubble, not an essay. If they ask something a specific skill's tutor chat would answer better (deep technical help on one topic), point them there instead of trying to teach the whole thing yourself.
            """;

    private final AssistantSessionRepository sessionRepo;
    private final AssistantMessageRepository messageRepo;
    private final RoadmapStepRepository stepRepo;
    private final SkillRepository skillRepo;
    private final CareerGoalService goalService;
    private final ProjectMemberRepository memberRepo;
    private final ProjectRepository projectRepo;
    private final GeminiClient geminiClient;

    public List<AssistantSessionResponse> listSessions(Long userId) {
        List<AssistantSession> sessions = sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
        Long activeId = sessions.isEmpty() ? null : sessions.get(0).getId();
        return sessions.stream().map(s -> toSessionResponse(s, activeId)).toList();
    }

    @Transactional
    public AssistantSessionResponse createSession(Long userId) {
        AssistantSession session = sessionRepo.save(
                AssistantSession.builder().userId(userId).build());
        return toSessionResponse(session, session.getId());
    }

    public List<AssistantMessageResponse> getMessages(Long userId, Long sessionId) {
        AssistantSession session = requireOwnedSession(userId, sessionId);
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageResponse).toList();
    }

    @Transactional
    public AssistantMessageResponse sendMessage(Long userId, Long sessionId, String userMessage) {
        AssistantSession session = requireOwnedSession(userId, sessionId);
        AssistantSession latest = sessionRepo.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (latest == null || !latest.getId().equals(session.getId())) {
            throw new ForbiddenException("This chat is read-only — start a new chat to keep talking to the AI.");
        }

        messageRepo.save(AssistantMessage.builder()
                .sessionId(sessionId).role(MessageRole.USER).content(userMessage).build());

        if (session.getTitle() == null) {
            session.setTitle(userMessage.length() > 60 ? userMessage.substring(0, 60) + "…" : userMessage);
            sessionRepo.save(session);
        }

        String systemInstruction = BASE_SYSTEM_INSTRUCTION + "\n\n" + buildUserContextSnippet(userId);
        List<ChatTurn> turns = recentTurns(sessionId);

        String reply = geminiClient.chat(systemInstruction, turns);

        AssistantMessage saved = messageRepo.save(AssistantMessage.builder()
                .sessionId(sessionId).role(MessageRole.ASSISTANT).content(reply).build());
        return toMessageResponse(saved);
    }

    private List<ChatTurn> recentTurns(Long sessionId) {
        List<AssistantMessage> all = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<AssistantMessage> recent = all.size() > MAX_CONTEXT_MESSAGES
                ? new ArrayList<>(all.subList(all.size() - MAX_CONTEXT_MESSAGES, all.size()))
                : new ArrayList<>(all);
        return recent.stream()
                .map(m -> m.getRole() == MessageRole.USER ? ChatTurn.user(m.getContent()) : ChatTurn.model(m.getContent()))
                .toList();
    }

    /**
     * Builds only the dynamic context relevant to this specific student.
     * Separated from the base system prompt to allow for static prompt caching.
     */
    private String buildUserContextSnippet(Long userId) {
        StringBuilder sb = new StringBuilder("THIS STUDENT'S CURRENT CONTEXT:\n");

        try {
            GapAnalysisResponse gap = goalService.getGapAnalysis(userId);
            sb.append("Career goal: \"").append(gap.getCareerRoleName())
              .append("\", ").append(gap.getProgressPercent()).append("% of required skills mastered. ");
        } catch (Exception ignored) {
            sb.append("No career goal set yet — if relevant, suggest they set one from Profile. ");
        }

        List<RoadmapStep> steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        
        if (!steps.isEmpty()) {
            Set<Long> skillIds = steps.stream().map(RoadmapStep::getSkillId).collect(Collectors.toSet());
            Map<Long, String> skillMap = skillRepo.findAllById(skillIds).stream()
                    .collect(Collectors.toMap(Skill::getId, Skill::getName));

            long done = steps.stream().filter(s -> s.getStatus() == StepStatus.DONE).count();
            List<String> doneNames = steps.stream()
                    .filter(s -> s.getStatus() == StepStatus.DONE)
                    .map(s -> skillMap.get(s.getSkillId()))
                    .filter(n -> n != null).toList();

            List<String> pendingNames = steps.stream()
                    .filter(s -> s.getStatus() != StepStatus.DONE)
                    .map(s -> skillMap.get(s.getSkillId()))
                    .filter(n -> n != null).toList();

            sb.append("Roadmap: ").append(done).append(" of ").append(steps.size()).append(" steps done. ");
            if (!doneNames.isEmpty()) sb.append("Already completed: ").append(String.join(", ", doneNames)).append(". ");
            if (!pendingNames.isEmpty()) sb.append("Still to do: ").append(String.join(", ", pendingNames)).append(". ");
        }

        List<Long> projectIds = memberRepo.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED).stream()
                .map(pm -> pm.getProjectId())
                .toList();

        if (!projectIds.isEmpty()) {
            List<String> projectNames = projectRepo.findAllById(projectIds).stream()
                    .map(p -> p.getName())
                    .toList();
            if (!projectNames.isEmpty()) {
                sb.append("Active projects: ").append(String.join(", ", projectNames)).append(". ");
            }
        }

        return sb.toString();
    }

    private AssistantSession requireOwnedSession(Long userId, Long sessionId) {
        AssistantSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        if (!session.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Chat session not found: " + sessionId);
        }
        return session;
    }

    private AssistantSessionResponse toSessionResponse(AssistantSession s, Long activeId) {
        String preview = messageRepo.findTopBySessionIdOrderByCreatedAtDesc(s.getId())
                .map(AssistantMessage::getContent)
                .map(c -> c.length() > 80 ? c.substring(0, 80) + "…" : c)
                .orElse(null);
        return AssistantSessionResponse.builder()
                .id(s.getId())
                .title(s.getTitle() != null ? s.getTitle() : "New chat")
                .createdAt(s.getCreatedAt())
                .active(s.getId().equals(activeId))
                .lastMessagePreview(preview)
                .build();
    }

    private AssistantMessageResponse toMessageResponse(AssistantMessage m) {
        return AssistantMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole().name())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}