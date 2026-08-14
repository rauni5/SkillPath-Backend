package com.skillpath.service;
import com.skillpath.ai.GeminiClient;
import com.skillpath.dto.response.DashboardSummaryResponse;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.StreakResponse;
import com.skillpath.model.DashboardSummary.DashboardSummary;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.StepStatus;
import com.skillpath.repository.DashboardSummaryRepository;
import com.skillpath.repository.ProjectMemberRepository;
import com.skillpath.repository.ProjectRepository;
import com.skillpath.repository.RoadmapStepRepository;
import com.skillpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates the short "what's next" summary shown at the top of the
 * dashboard. Unlike the tutor/roadmap chats, this is a single one-shot
 * Gemini call (no back-and-forth) triggered only when the student taps
 * "Refresh" — the result is cached in {@link DashboardSummary} so it
 * survives app restarts until they regenerate it.
 */
@Service @RequiredArgsConstructor
public class DashboardAiService {
    private final DashboardSummaryRepository summaryRepo;
    private final CareerGoalService goalService;
    private final RoadmapStepRepository stepRepo;
    private final SkillRepository skillRepo;
    private final ProjectMemberRepository memberRepo;
    private final ProjectRepository projectRepo;
    private final GamificationService gamificationService;
    private final GeminiClient geminiClient;

    public Optional<DashboardSummaryResponse> getSummary(Long userId) {
        return summaryRepo.findByUserId(userId).map(this::toResponse);
    }

    @Transactional
    public DashboardSummaryResponse generateSummary(Long userId) {
        String prompt = buildPrompt(userId);
        String systemInstruction =
                "You are an encouraging AI career coach inside a student learning app called SkillPath. " +
                "Given a snapshot of a student's progress, write a short summary (3-5 sentences, plain " +
                "text, no markdown or headers) of where they currently stand and what their single most " +
                "important next step should be. Reference the actual skill, project, or role names you're " +
                "given — be specific, not generic. Keep the tone warm and motivating, not robotic.";

        String content = geminiClient.chat(systemInstruction, List.of(
                com.skillpath.ai.ChatTurn.user(prompt)
        ));

        DashboardSummary saved = summaryRepo.save(DashboardSummary.builder()
                .userId(userId)
                .content(content.trim())
                .generatedAt(Instant.now())
                .build());
        return toResponse(saved);
    }

    private String buildPrompt(Long userId) {
        StringBuilder sb = new StringBuilder();

        String roleName = null;
        int progress = 0;
        try {
            GapAnalysisResponse gap = goalService.getGapAnalysis(userId);
            roleName = gap.getCareerRoleName();
            progress = gap.getProgressPercent();
        } catch (Exception ignored) {
            // no career goal set yet
        }
        sb.append("Career goal: ").append(roleName != null ? roleName : "not set yet").append(".\n");
        sb.append("Career progress toward that goal: ").append(progress).append("%.\n");

        List<RoadmapStep> steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        long done = steps.stream().filter(s -> s.getStatus() == StepStatus.DONE).count();
        sb.append("Roadmap: ").append(done).append(" of ").append(steps.size()).append(" steps completed.\n");

        List<String> nextSkills = steps.stream()
                .filter(s -> s.getStatus() != StepStatus.DONE)
                .limit(3)
                .map(s -> skillRepo.findById(s.getSkillId()).map(Skill::getName).orElse(null))
                .filter(n -> n != null)
                .collect(Collectors.toList());
        sb.append("Next skills up in the roadmap: ")
          .append(nextSkills.isEmpty() ? "none — roadmap is complete or empty" : String.join(", ", nextSkills))
          .append(".\n");

        List<String> activeProjectNames = memberRepo.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED).stream()
                .map(pm -> projectRepo.findById(pm.getProjectId()).map(proj -> proj.getName()).orElse(null))
                .filter(n -> n != null)
                .collect(Collectors.toList());
        sb.append("Active projects: ")
          .append(activeProjectNames.isEmpty() ? "none currently" : String.join(", ", activeProjectNames))
          .append(".\n");

        try {
            StreakResponse streak = gamificationService.getStreak(userId);
            sb.append("Current activity streak: ").append(streak.getCurrentStreak()).append(" day(s).\n");
        } catch (Exception ignored) {
        }

        return sb.toString();
    }

    private DashboardSummaryResponse toResponse(DashboardSummary s) {
        return DashboardSummaryResponse.builder()
                .content(s.getContent())
                .generatedAt(s.getGeneratedAt())
                .build();
    }
}