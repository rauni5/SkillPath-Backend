package com.skillpath.service;
import com.skillpath.dto.request.SetCareerGoalRequest;
import com.skillpath.dto.request.SwitchBranchRequest;
import com.skillpath.dto.response.BranchRecommendationResponse;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.GapAnalysisResponse.MissingSkill;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkill;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleBranch.RoleBranch;
import com.skillpath.model.RoleRequiredSkill.RoleRequiredSkill;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.UserCareerGoal.UserCareerGoal;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.*;

@Service @RequiredArgsConstructor
public class CareerGoalService {
    private final UserCareerGoalRepository goalRepo;
    private final CareerRoleRepository roleRepo;
    private final RoleRequiredSkillRepository roleSkillRepo;
    private final RoleBranchRepository branchRepo;
    private final BranchRequiredSkillRepository branchSkillRepo;
    private final UserSkillRepository userSkillRepo;
    private final SkillRepository skillRepo;
    private final RoadmapService roadmapService;

    @Transactional
    public void setGoal(Long userId, SetCareerGoalRequest req) {
        CareerRole role = roleRepo.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found:" + req.getRoleId()));

        List<RoleBranch> branches = branchRepo.findByRoleId(role.getId());
        Long resolvedBranchId = resolveBranch(userId, req.getRoleId(), req.getBranchId(), branches);

        UserCareerGoal goal = goalRepo.findByUserId(userId)
                                            .orElse(UserCareerGoal.builder().userId(userId).build());
        goal.setRoleId(req.getRoleId());
        goal.setBranchId(resolvedBranchId);
        goal.setSetAt(Instant.now());
        goalRepo.save(goal);
        roadmapService.generateRoadmap(userId);
    }

    @Transactional
    public void switchBranch(Long userId, SwitchBranchRequest req) {
        UserCareerGoal goal = goalRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No career goal set for user: " + userId));
        RoleBranch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + req.getBranchId()));
        if (!branch.getRoleId().equals(goal.getRoleId()))
            throw new ForbiddenException("That branch doesn't belong to your current career goal.");

        goal.setBranchId(branch.getId());
        goal.setSetAt(Instant.now());
        goalRepo.save(goal);
        roadmapService.generateRoadmap(userId);
    }

    /** Ranks a role's branches by overlap with the user's current skills — highest first. */
    public List<BranchRecommendationResponse> recommendBranches(Long userId, Long roleId) {
        List<RoleBranch> branches = branchRepo.findByRoleId(roleId);
        Set<Long> userSkills = userSkillRepo.findSkillIdsByUserId(userId);
        return branches.stream()
                .map(b -> {
                    Set<Long> required = branchSkillRepo.findSkillIdsByBranchId(b.getId());
                    double score = Math.round(jaccard(userSkills, required) * 100.0);
                    return BranchRecommendationResponse.builder()
                            .branchId(b.getId())
                            .name(b.getName())
                            .description(b.getDescription())
                            .matchScore(score)
                            .build();
                })
                .sorted(Comparator.comparingDouble(BranchRecommendationResponse::getMatchScore).reversed())
                .toList();
    }

    public GapAnalysisResponse getGapAnalysis(Long userId) {
        UserCareerGoal goal = goalRepo.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("No career goal set for user: "+userId));
        CareerRole role = roleRepo.findById(goal.getRoleId()).orElseThrow();

        Set<Long> required;
        String branchName = null;
        if (goal.getBranchId() != null) {
            RoleBranch branch = branchRepo.findById(goal.getBranchId()).orElse(null);
            if (branch != null) {
                branchName = branch.getName();
                required = branchSkillRepo.findSkillIdsByBranchId(branch.getId());
                Set<Long> userHas = userSkillRepo.findSkillIdsByUserId(userId);
                return buildGapResponse(role, goal.getBranchId(), branchName, required, userHas,
                        branchSkillRepo.findByBranchId(branch.getId()).stream()
                                .collect(Collectors.toMap(BranchRequiredSkill::getSkillId, BranchRequiredSkill::getImportance)));
            }
        }
        // No branches for this role, or branch was deleted out from under the goal — fall back to the role's flat list.
        required = roleSkillRepo.findSkillIdsByRoleId(role.getId());
        Set<Long> userHas = userSkillRepo.findSkillIdsByUserId(userId);
        Map<Long, Integer> importanceBySkill = roleSkillRepo.findByRoleId(role.getId()).stream()
                .collect(Collectors.toMap(RoleRequiredSkill::getSkillId, RoleRequiredSkill::getImportance));
        return buildGapResponse(role, null, null, required, userHas, importanceBySkill);
    }

    private GapAnalysisResponse buildGapResponse(CareerRole role, Long branchId, String branchName,
            Set<Long> required, Set<Long> userHas, Map<Long, Integer> importanceBySkill) {
        Set<Long> missing = new HashSet<>(required);
        missing.removeAll(userHas);
        int progress = required.isEmpty() ? 100 : (int) ((double) (required.size() - missing.size()) / required.size() * 100);
        List<MissingSkill> list = missing.stream()
                .sorted(Comparator.comparingInt((Long id) -> importanceBySkill.getOrDefault(id, 0)).reversed())
                .map(id -> new MissingSkill(id, skillRepo.findById(id).map(Skill::getName).orElse("?"),
                        importanceBySkill.getOrDefault(id, 0)))
                .collect(Collectors.toList());
        return GapAnalysisResponse.builder()
                                .careerRoleName(role.getName())
                                .branchId(branchId)
                                .branchName(branchName)
                                .progressPercent(progress)
                                .knownSkillCount(required.size()-missing.size())
                                .requiredSkillCount(required.size())
                                .missingSkills(list).build();
    }

    private Long resolveBranch(Long userId, Long roleId, Long requestedBranchId, List<RoleBranch> branches) {
        if (branches.isEmpty()) return null;
        if (requestedBranchId != null) {
            boolean valid = branches.stream().anyMatch(b -> b.getId().equals(requestedBranchId));
            if (!valid) throw new ResourceNotFoundException("Branch not found for this role: " + requestedBranchId);
            return requestedBranchId;
        }
        List<BranchRecommendationResponse> ranked = recommendBranches(userId, roleId);
        return ranked.isEmpty() ? null : ranked.get(0).getBranchId();
    }

    private double jaccard(Set<Long> a, Set<Long> b) {
        if (b == null || b.isEmpty()) return 0.0;
        Set<Long> inter = new HashSet<>(a);
        inter.retainAll(b);
        Set<Long> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }
}