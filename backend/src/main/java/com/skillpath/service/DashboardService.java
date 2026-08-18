package com.skillpath.service;
import com.skillpath.dto.response.*;
import com.skillpath.model.enums.*;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class DashboardService {
    private final CareerGoalService goalService;
    private final RoadmapStepRepository stepRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final SkillRepository skillRepo;
    public DashboardResponse getDashboard(Long userId) {
        // Career progress — 0% if no goal set
        int progress = 0; String roleName = null;
        int knownSkillCount = 0; int requiredSkillCount = 0;
        try {
            GapAnalysisResponse gap = goalService.getGapAnalysis(userId);
            progress = gap.getProgressPercent();
            roleName = gap.getCareerRoleName();
            knownSkillCount = gap.getKnownSkillCount();
            requiredSkillCount = gap.getRequiredSkillCount();
        } catch (Exception ignored) {}
        // Roadmap stats
        var steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        long done = steps.stream()
        .filter(s -> s.getStatus() == StepStatus.DONE).count();
        // Next 3 pending skills
        List<SkillResponse> nextSkills = steps.stream()
                                            .filter(s -> s.getStatus() == StepStatus.PENDING)
                                            .limit(3)
                                            .map(s -> skillRepo.findById(s.getSkillId())
                                            .map(SkillResponse::from).orElseThrow())
                                            .toList();
        // Active projects the user is accepted into
        List<ProjectResponse> active = memberRepo
                                            .findByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                                            .stream()
                                            .map(pm -> projectRepo.findById(pm.getProjectId())
                                            .map(ProjectResponse::from).orElseThrow())
                                            .toList();
        return DashboardResponse.builder()
                            .careerProgressPercent(progress)
                            .careerRoleName(roleName)
                            .knownSkillCount(knownSkillCount)
                            .requiredSkillCount(requiredSkillCount)
                            .roadmapCompletedSteps((int) done)
                            .roadmapTotalSteps(steps.size())
                            .nextSkillsToLearn(nextSkills)
                            .activeProjects(active).build();
    }
}