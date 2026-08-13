package com.skillpath.service;
import com.skillpath.ai.ChatTurn;
import com.skillpath.ai.GeminiClient;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.RoadmapChatMessageResponse;
import com.skillpath.dto.response.RoadmapChatSessionResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.RoadmapChatMessage.RoadmapChatMessage;
import com.skillpath.model.RoadmapChatSession.RoadmapChatSession;
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

/**
 * A general, roadmap-wide AI chat (as opposed to the per-skill tutor chat).
 * Organized into sessions like a normal chat app: starting a new session
 * effectively "closes" the previous one to further replies, but every past
 * session and its messages stay readable.
 */
@Service @RequiredArgsConstructor
public class RoadmapChatService {
    private static final int MAX_CONTEXT_MESSAGES = 8;

    private final RoadmapChatSessionRepository sessionRepo;
    private final RoadmapChatMessageRepository messageRepo;
    private final RoadmapStepRepository stepRepo;
    private final SkillRepository skillRepo;
    private final CareerGoalService goalService;
    private final ProjectMemberRepository memberRepo;
    private final ProjectRepository projectRepo;
    private final GeminiClient geminiClient;

    public List<RoadmapChatSessionResponse> listSessions(Long userId) {
        List<RoadmapChatSession> sessions = sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
        Long activeId = sessions.isEmpty() ? null : sessions.get(0).getId();
        return sessions.stream().map(s -> toSessionResponse(s, activeId)).toList();
    }

    @Transactional
    public RoadmapChatSessionResponse createSession(Long userId) {
        RoadmapChatSession session = sessionRepo.save(
                RoadmapChatSession.builder().userId(userId).build());
        return toSessionResponse(session, session.getId());
    }

    public List<RoadmapChatMessageResponse> getMessages(Long userId, Long sessionId) {
        RoadmapChatSession session = requireOwnedSession(userId, sessionId);
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageResponse).toList();
    }

    @Transactional
    public RoadmapChatMessageResponse sendMessage(Long userId, Long sessionId, String userMessage) {
        RoadmapChatSession session = requireOwnedSession(userId, sessionId);
        RoadmapChatSession latest = sessionRepo.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (latest == null || !latest.getId().equals(session.getId())) {
            throw new ForbiddenException("This chat is read-only — start a new chat to keep talking to the AI.");
        }

        messageRepo.save(RoadmapChatMessage.builder()
                .sessionId(sessionId).role(MessageRole.USER).content(userMessage).build());

        if (session.getTitle() == null) {
            session.setTitle(userMessage.length() > 60 ? userMessage.substring(0, 60) + "…" : userMessage);
            sessionRepo.save(session);
        }

        String systemInstruction = buildSystemInstruction(userId);
        List<ChatTurn> turns = recentTurns(sessionId);

        String reply = geminiClient.chat(systemInstruction, turns);

        RoadmapChatMessage saved = messageRepo.save(RoadmapChatMessage.builder()
                .sessionId(sessionId).role(MessageRole.ASSISTANT).content(reply).build());
        return toMessageResponse(saved);
    }

    private List<ChatTurn> recentTurns(Long sessionId) {
        List<RoadmapChatMessage> all = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<RoadmapChatMessage> recent = all.size() > MAX_CONTEXT_MESSAGES
                ? new ArrayList<>(all.subList(all.size() - MAX_CONTEXT_MESSAGES, all.size()))
                : new ArrayList<>(all);
        return recent.stream()
                .map(m -> m.getRole() == MessageRole.USER ? ChatTurn.user(m.getContent()) : ChatTurn.model(m.getContent()))
                .toList();
    }

    private String buildSystemInstruction(Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a friendly AI guide helping a student navigate their learning roadmap inside " +
                "the SkillPath app. Unlike the per-skill tutor, you talk about the roadmap as a whole — " +
                "career direction, sequencing, motivation, and how pieces fit together. ");

        try {
            GapAnalysisResponse gap = goalService.getGapAnalysis(userId);
            sb.append("Their career goal is \"").append(gap.getCareerRoleName())
              .append("\", currently at ").append(gap.getProgressPercent()).append("% progress. ");
        } catch (Exception ignored) {
            sb.append("They haven't set a career goal yet. ");
        }

        List<RoadmapStep> steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        long done = steps.stream().filter(s -> s.getStatus() == StepStatus.DONE).count();
        List<String> doneNames = steps.stream().filter(s -> s.getStatus() == StepStatus.DONE)
                .map(s -> skillRepo.findById(s.getSkillId()).map(Skill::getName).orElse(null))
                .filter(n -> n != null).toList();
        List<String> pendingNames = steps.stream().filter(s -> s.getStatus() != StepStatus.DONE)
                .map(s -> skillRepo.findById(s.getSkillId()).map(Skill::getName).orElse(null))
                .filter(n -> n != null).toList();
        sb.append("Roadmap: ").append(done).append(" of ").append(steps.size()).append(" steps done. ");
        if (!doneNames.isEmpty()) sb.append("Already completed: ").append(String.join(", ", doneNames)).append(". ");
        if (!pendingNames.isEmpty()) sb.append("Still to do: ").append(String.join(", ", pendingNames)).append(". ");

        List<String> projectNames = memberRepo.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED).stream()
                .map(pm -> projectRepo.findById(pm.getProjectId()).map(p -> p.getName()).orElse(null))
                .filter(n -> n != null).toList();
        if (!projectNames.isEmpty()) sb.append("Active projects: ").append(String.join(", ", projectNames)).append(". ");

        sb.append("Keep replies concise, practical and encouraging. If they ask about a specific skill in " +
                "depth, gently suggest they open that skill's dedicated tutor chat from the roadmap for a " +
                "focused lesson — you're here for the bigger picture.");
        return sb.toString();
    }

    private RoadmapChatSession requireOwnedSession(Long userId, Long sessionId) {
        RoadmapChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        if (!session.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Chat session not found: " + sessionId);
        }
        return session;
    }

    private RoadmapChatSessionResponse toSessionResponse(RoadmapChatSession s, Long activeId) {
        String preview = messageRepo.findTopBySessionIdOrderByCreatedAtDesc(s.getId())
                .map(RoadmapChatMessage::getContent)
                .map(c -> c.length() > 80 ? c.substring(0, 80) + "…" : c)
                .orElse(null);
        return RoadmapChatSessionResponse.builder()
                .id(s.getId())
                .title(s.getTitle() != null ? s.getTitle() : "New chat")
                .createdAt(s.getCreatedAt())
                .active(s.getId().equals(activeId))
                .lastMessagePreview(preview)
                .build();
    }

    private RoadmapChatMessageResponse toMessageResponse(RoadmapChatMessage m) {
        return RoadmapChatMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole().name())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}