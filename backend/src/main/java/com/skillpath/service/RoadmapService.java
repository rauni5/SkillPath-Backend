package com.skillpath.service;
import com.skillpath.algorithm.graph.SkillGraph;
import com.skillpath.algorithm.graph.TopologicalSort;
import com.skillpath.dto.response.RoadmapStepResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.RoadmapStep.RoadmapStep;
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
    private final RoleRequiredSkillRepository roleSkillRepo;
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
        // 3. Load what the target role requires
        Long roleId = goalRepo.findRoleIdByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("No career goal set for user: " + userId));
        Set<Long> required = roleSkillRepo.findSkillIdsByRoleId(roleId);
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
    public RoadmapStepResponse markDone(Long stepId) {
        RoadmapStep step = stepRepo.findById(stepId)
        .orElseThrow(() -> new ResourceNotFoundException("Step not found:"+stepId));
        step.setStatus(StepStatus.DONE);
        step.setCompletedAt(Instant.now());
        return toResponse(stepRepo.save(step));
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