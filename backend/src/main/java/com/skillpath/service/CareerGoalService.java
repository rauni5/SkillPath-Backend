package com.skillpath.service;
import com.skillpath.dto.request.SetCareerGoalRequest;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.GapAnalysisResponse.MissingSkill;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.CareerRole.CareerRole;
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
    private final UserSkillRepository userSkillRepo;
    private final SkillRepository skillRepo;
    @Transactional
    public void setGoal(Long userId, SetCareerGoalRequest req) {
        if (!roleRepo.existsById(req.getRoleId())) throw new ResourceNotFoundException("Role not found:"+req.getRoleId());
        UserCareerGoal goal = goalRepo.findByUserId(userId)
                                            .orElse(UserCareerGoal.builder().userId(userId).build());
        goal.setRoleId(req.getRoleId());
        goal.setSetAt(Instant.now());
        goalRepo.save(goal);
    }
    public GapAnalysisResponse getGapAnalysis(Long userId) {
        Long roleId = goalRepo.findRoleIdByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("No career goal set for user: "+userId));
        CareerRole role = roleRepo.findById(roleId).orElseThrow();
        Set<Long> required = roleSkillRepo.findSkillIdsByRoleId(roleId);
        Set<Long> userHas = userSkillRepo.findSkillIdsByUserId(userId);
        Set<Long> missing = new HashSet<>(required);
        missing.removeAll(userHas);
        int progress = required.isEmpty() ? 100: (int)((double)(required.size()-missing.size())/required.size()*100);
        List<MissingSkill> list = roleSkillRepo.findByRoleId(roleId).stream()
                                            .filter(rs -> missing.contains(rs.getSkillId()))
                                            .sorted(Comparator.comparingInt(RoleRequiredSkill::getImportance).reversed())
                                            .map(rs -> new MissingSkill(rs.getSkillId(),
                                            skillRepo.findById(rs.getSkillId())
                                            .map(Skill::getName).orElse("?"),
                                            rs.getImportance()))
                                            .collect(Collectors.toList());
        return GapAnalysisResponse.builder()
                                .careerRoleName(role.getName())
                                .progressPercent(progress)
                                .knownSkillCount(required.size()-missing.size())
                                .requiredSkillCount(required.size())
                                .missingSkills(list).build();
    }
}