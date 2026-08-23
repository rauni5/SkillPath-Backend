package com.skillpath.service;
import com.skillpath.algorithm.graph.SkillGraph;
import com.skillpath.algorithm.graph.TopologicalSort;
import com.skillpath.dto.response.RoadmapStepResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import com.skillpath.model.UserCareerGoal.UserCareerGoal;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.model.UserSkill.UserSkillId;
import com.skillpath.model.enums.Proficiency;
import com.skillpath.model.enums.StepStatus;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Service @RequiredArgsConstructor
public class RoadmapService {
    private final SkillDependencyRepository depRepo;
    private final UserSkillRepository userSkillRepo;
    private final UserCareerGoalRepository goalRepo;
    private final BranchRequiredSkillRepository branchSkillRepo;
    private final RoadmapStepRepository stepRepo;
    private final SkillRepository skillRepo;
    @Transactional
    public List<RoadmapStepResponse> generateRoadmap(Long userId) {
        // 1. Build skill graph from the database
        SkillGraph graph = new SkillGraph();
        depRepo.findAllDependencies().forEach(dep -> {
                                                    graph.addSkill(dep.getSkillId());
                                                    graph.addSkill(dep.getPrerequisiteId());
                                                    graph.addDependency(dep.getSkillId(), dep.getPrerequisiteId());
        });
        // 2. Load what the user already knows
        Set<Long> userSkills = userSkillRepo.findSkillIdsByUserId(userId);
        // 3. Load what the selected branch requires
        UserCareerGoal goal = goalRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No career goal set for user: " + userId));
        if (goal.getBranchId() == null)
            throw new IllegalStateException(
                "This career goal has no branch selected yet — choose a branch before generating a roadmap.");
        Set<Long> required = branchSkillRepo.findSkillIdsByBranchId(goal.getBranchId());
        // 4. Find every skill still needed (including transitive prerequisites)
        Set<Long> missing = graph.getMissingSkills(userSkills, required);
        // 5. Topological sort — valid learning sequence
        List<Long> ordered = new TopologicalSort().sort(graph, missing);
        // 6. Persist as roadmap steps
        stepRepo.deleteByUserId(userId);
        List<RoadmapStep> steps = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++)
            steps.add(RoadmapStep.builder()
                .userId(userId)
                .skillId(ordered.get(i))
                .stepOrder(i)
                .status(StepStatus.PENDING).build());
        return toResponses(stepRepo.saveAll(steps));
    }
    public List<RoadmapStepResponse> getRoadmap(Long userId) {
        List<RoadmapStep> steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        if (steps.isEmpty()) return generateRoadmap(userId);
        return toResponses(steps);
    }
    @Transactional
    public RoadmapStepResponse markDone(Long userId, Long stepId) {
        RoadmapStep step = stepRepo.findById(stepId)
        .orElseThrow(() -> new ResourceNotFoundException("Step not found:"+stepId));
        if (!step.getUserId().equals(userId))
            throw new ResourceNotFoundException("Step not found:"+stepId);
        step.setStatus(StepStatus.DONE);
        step.setCompletedAt(Instant.now());
        // Completing a roadmap step means the user now knows this skill —
        // add it to their profile (default Beginner) if they don't have it yet.
        upsertUserSkillIfAbsent(userId, step.getSkillId(), Proficiency.BEGINNER);
        return toResponse(stepRepo.save(step));
    }

    /**
     * Called when a skill check is passed. Marks any pending roadmap step(s)
     * for that skill as done and records the proficiency actually earned on
     * the quiz, rather than defaulting to Beginner. Safe to call even if no
     * roadmap step exists for this skill (e.g. skill checks taken outside a
     * roadmap context) — it just updates the skill profile in that case.
     */
    @Transactional
    public void completeStepsForSkill(Long userId, Long skillId, Proficiency earnedProficiency) {
        Instant now = Instant.now();
        markStepsDone(userId, skillId, now);
        upsertUserSkillOrRaise(userId, skillId, earnedProficiency);

        SkillGraph graph = new SkillGraph();
        depRepo.findAllDependencies().forEach(dep -> {
            graph.addSkill(dep.getSkillId());
            graph.addSkill(dep.getPrerequisiteId());
            graph.addDependency(dep.getSkillId(), dep.getPrerequisiteId());
        });
        for (Long prereqId : graph.getAllPrerequisites(skillId)) {
            upsertUserSkillIfAbsent(userId, prereqId, Proficiency.BEGINNER);
            markStepsDone(userId, prereqId, now);
        }
    }

    private void markStepsDone(Long userId, Long skillId, Instant now) {
        List<RoadmapStep> steps = stepRepo.findByUserIdAndSkillId(userId, skillId);
        for (RoadmapStep step : steps) {
            if (step.getStatus() != StepStatus.DONE) {
                step.setStatus(StepStatus.DONE);
                step.setCompletedAt(now);
                stepRepo.save(step);
            }
        }
    }

    private void upsertUserSkillIfAbsent(Long userId, Long skillId, Proficiency proficiency) {
        var id = new UserSkillId(userId, skillId);
        if (!userSkillRepo.existsById(id)) {
            userSkillRepo.save(UserSkill.builder()
                .userId(userId).skillId(skillId).proficiency(proficiency).build());
        }
    }

    /** Unlike markDone's default, a skill-check result should always set the
     * proficiency the user actually just earned — including raising an
     * existing lower proficiency, since they've now demonstrated more. */
    private void upsertUserSkillOrRaise(Long userId, Long skillId, Proficiency proficiency) {
        var id = new UserSkillId(userId, skillId);
        UserSkill existing = userSkillRepo.findById(id).orElse(null);
        if (existing == null) {
            userSkillRepo.save(UserSkill.builder()
                .userId(userId).skillId(skillId).proficiency(proficiency).build());
        } else if (proficiency.ordinal() > existing.getProficiency().ordinal()) {
            existing.setProficiency(proficiency);
            userSkillRepo.save(existing);
        }
    }
    private List<RoadmapStepResponse> toResponses(List<RoadmapStep> steps) {
        return steps.stream().map(this::toResponse).toList();
    }
    private RoadmapStepResponse toResponse(RoadmapStep s) {
        var skill = skillRepo.findById(s.getSkillId()).orElseThrow();
        return RoadmapStepResponse.from(s, skill.getName(),
        skill.getCategory().name());
    }
}