package com.skillpath.service;

import com.skillpath.dto.request.*;
import com.skillpath.dto.response.AchievementDeletionResult;
import com.skillpath.dto.response.AdminAchievementResponse;
import com.skillpath.dto.response.RoleRequirementResponse;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Achievement.Achievement;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleRequiredSkill.RoleRequiredSkill;
import com.skillpath.model.RoleRequiredSkill.RoleRequiredSkillId;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.SkillDependency.SkillDependency;
import com.skillpath.model.SkillDependency.SkillDependencyId;
import com.skillpath.model.User.User;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SkillRepository             skillRepo;
    private final SkillDependencyRepository   depRepo;
    private final CareerRoleRepository        roleRepo;
    private final RoleRequiredSkillRepository roleSkillRepo;
    private final UserRepository              userRepo;
    private final SkillTrieService            trieService;
    private final AchievementRepository       achievementRepo;
    private final UserAchievementRepository   userAchievementRepo;

    
    public SkillResponse getSkill(Long skillId) {
        return skillRepo.findById(skillId)
                .map(SkillResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
    }

    @Transactional
    public SkillResponse updateSkill(Long skillId, CreateSkillRequest req) {
        Skill skill = skillRepo.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
        boolean nameTaken = skillRepo.existsByNameIgnoreCase(req.getName())
                && !skill.getName().equalsIgnoreCase(req.getName());
        if (nameTaken)
            throw new IllegalArgumentException(
                "A skill named '" + req.getName() + "' already exists.");

        skill.setName(req.getName());
        skill.setCategory(req.getCategory());
        skill.setDescription(req.getDescription());
        Skill saved = skillRepo.save(skill);

        // Best-effort: the live Trie only supports inserts, so a rename adds
        // a new entry under the new name; the old name lingers until the
        // next app restart (which rebuilds the Trie fresh from the DB).
        trieService.insertSkill(saved.getName(), saved.getId());

        return SkillResponse.from(saved);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        if (!skillRepo.existsById(skillId))
            throw new ResourceNotFoundException("Skill not found: " + skillId);
        try {
            skillRepo.deleteById(skillId);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Can't delete this skill — it's still required by at least one project. " +
                "Remove it from those projects first.");
        }
    }

    @Transactional
    public SkillResponse createSkill(CreateSkillRequest req) {
        if (skillRepo.existsByNameIgnoreCase(req.getName()))
            throw new IllegalArgumentException(
                "A skill named '" + req.getName() + "' already exists.");

        Skill skill = Skill.builder()
                .name(req.getName())
                .category(req.getCategory())
                .description(req.getDescription())
                .build();
        Skill saved = skillRepo.save(skill);

        // Keep the live Trie in sync — no restart required
        trieService.insertSkill(saved.getName(), saved.getId());

        return SkillResponse.from(saved);
    }

    
    @Transactional
    public void addDependency(Long skillId, Long prerequisiteId) {
        if (!skillRepo.existsById(skillId))
            throw new ResourceNotFoundException("Skill not found: " + skillId);
        if (!skillRepo.existsById(prerequisiteId))
            throw new ResourceNotFoundException("Prerequisite not found: " + prerequisiteId);
        if (skillId.equals(prerequisiteId))
            throw new IllegalArgumentException("A skill cannot be its own prerequisite.");

        // Cycle detection: if skillId is already reachable from prerequisiteId,
        Set<Long> reachableFromPrereq = getAllPrerequisiteIds(prerequisiteId);
        if (reachableFromPrereq.contains(skillId))
            throw new IllegalArgumentException(
                "Adding this dependency would create a cycle in the skill graph.");

        boolean alreadyExists = depRepo.findBySkillId(skillId)
                .stream().anyMatch(d -> d.getPrerequisiteId().equals(prerequisiteId));
        if (alreadyExists)
            throw new IllegalArgumentException("This dependency already exists.");

        depRepo.save(SkillDependency.builder()
                .skillId(skillId)
                .prerequisiteId(prerequisiteId)
                .build());
    }


    @Transactional
    public void removeDependency(Long skillId, Long prerequisiteId) {
        SkillDependencyId id = new SkillDependencyId(skillId, prerequisiteId);
        if (!depRepo.existsById(id))
            throw new ResourceNotFoundException(
                "Dependency not found: " + skillId + " → " + prerequisiteId);
        depRepo.deleteById(id);
    }

    /** List all prerequisite skill IDs for a given skill. */
    public List<SkillResponse> getDependencies(Long skillId) {
        if (!skillRepo.existsById(skillId))
            throw new ResourceNotFoundException("Skill not found: " + skillId);
        return depRepo.findBySkillId(skillId).stream()
                .map(dep -> skillRepo.findById(dep.getPrerequisiteId())
                        .map(SkillResponse::from)
                        .orElseThrow())
                .toList();
    }

    @Transactional
    public CareerRole createCareerRole(CreateCareerRoleRequest req) {
        CareerRole role = CareerRole.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
        return roleRepo.save(role);
    }

    public CareerRole getCareerRole(Long roleId) {
        return roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found: " + roleId));
    }

    @Transactional
    public CareerRole updateCareerRole(Long roleId, CreateCareerRoleRequest req) {
        CareerRole role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found: " + roleId));
        boolean nameTaken = roleRepo.findByName(req.getName())
                .map(existing -> !existing.getId().equals(roleId))
                .orElse(false);
        if (nameTaken)
            throw new IllegalArgumentException(
                "A career role named '" + req.getName() + "' already exists.");
        role.setName(req.getName());
        role.setDescription(req.getDescription());
        return roleRepo.save(role);
    }

    @Transactional
    public void deleteCareerRole(Long roleId) {
        if (!roleRepo.existsById(roleId))
            throw new ResourceNotFoundException("Career role not found: " + roleId);
        try {
            roleRepo.deleteById(roleId);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Can't delete this role — at least one user has it as their career goal, " +
                "or a project currently requires it. Those must change first.");
        }
    }

    @Transactional
    public void addRoleRequirement(Long roleId, AddRequirementRequest req) {
        if (!roleRepo.existsById(roleId))
            throw new ResourceNotFoundException("Career role not found: " + roleId);
        if (!skillRepo.existsById(req.getSkillId()))
            throw new ResourceNotFoundException("Skill not found: " + req.getSkillId());

        boolean exists = roleSkillRepo.findByRoleId(roleId).stream()
                .anyMatch(r -> r.getSkillId().equals(req.getSkillId()));
        if (exists)
            throw new IllegalArgumentException(
                "This skill is already a requirement for this role.");

        roleSkillRepo.save(RoleRequiredSkill.builder()
                .roleId(roleId)
                .skillId(req.getSkillId())
                .importance(req.getImportance())
                .build());
    }

    @Transactional
    public void updateRoleRequirement(Long roleId, Long skillId, AddRequirementRequest req) {
        RoleRequiredSkillId id = new RoleRequiredSkillId(roleId, skillId);
        RoleRequiredSkill existing = roleSkillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Requirement not found for role " + roleId + " / skill " + skillId));
        existing.setImportance(req.getImportance());
        roleSkillRepo.save(existing);
    }

    @Transactional
    public void removeRoleRequirement(Long roleId, Long skillId) {
        RoleRequiredSkillId id = new RoleRequiredSkillId(roleId, skillId);
        if (!roleSkillRepo.existsById(id))
            throw new ResourceNotFoundException(
                "Requirement not found for role " + roleId + " / skill " + skillId);
        roleSkillRepo.deleteById(id);
    }

    public List<RoleRequirementResponse> getRoleRequirements(Long roleId) {
        if (!roleRepo.existsById(roleId))
            throw new ResourceNotFoundException("Career role not found: " + roleId);
        return roleSkillRepo.findByRoleId(roleId).stream()
                .map(r -> {
                    Skill skill = skillRepo.findById(r.getSkillId()).orElseThrow();
                    return RoleRequirementResponse.builder()
                            .skillId(skill.getId())
                            .name(skill.getName())
                            .category(skill.getCategory().name())
                            .importance(r.getImportance())
                            .build();
                })
                .toList();
    }

    public List<UserResponse> listAllUsers() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setAdminFlag(Long userId, boolean isAdmin) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found: " + userId));
        user.setAdmin(isAdmin);
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public UserResponse bootstrapFirstAdmin(String firebaseUid) {
        boolean adminAlreadyExists = userRepo.findAll()
                .stream().anyMatch(User::isAdmin);
        if (adminAlreadyExists)
            throw new IllegalStateException(
                "An admin already exists. Use /admin/users/{id}/admin to manage roles.");

        User user = userRepo.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found for UID: " + firebaseUid));
        user.setAdmin(true);
        return UserResponse.from(userRepo.save(user));
    }


    // ACHIEVEMENT MANAGEMENT

    public List<AdminAchievementResponse> listAchievements() {
        return achievementRepo.findAll().stream()
                .map(a -> AdminAchievementResponse.from(a, userAchievementRepo.countByAchievementId(a.getId())))
                .toList();
    }

    public AdminAchievementResponse getAchievementAdmin(Long achievementId) {
        Achievement a = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));
        return AdminAchievementResponse.from(a, userAchievementRepo.countByAchievementId(a.getId()));
    }

    @Transactional
    public AdminAchievementResponse createAchievement(CreateAchievementRequest req) {
        String code = req.getCode().trim().toUpperCase();
        if (achievementRepo.existsByCodeIgnoreCase(code))
            throw new IllegalArgumentException(
                "An achievement with code '" + code + "' already exists.");

        Achievement achievement = Achievement.builder()
                .code(code)
                .title(req.getTitle().trim())
                .description(req.getDescription().trim())
                .icon(req.getIcon().trim())
                .category(req.getCategory().trim())
                .criteriaType(req.getCriteriaType())
                .criteriaValue(req.getCriteriaValue())
                .enabled(true)
                .build();
        Achievement saved = achievementRepo.save(achievement);
        return AdminAchievementResponse.from(saved, 0);
    }

    @Transactional
    public AdminAchievementResponse updateAchievement(Long achievementId, UpdateAchievementRequest req) {
        Achievement achievement = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));

        achievement.setTitle(req.getTitle().trim());
        achievement.setDescription(req.getDescription().trim());
        achievement.setIcon(req.getIcon().trim());
        achievement.setCategory(req.getCategory().trim());
        achievement.setCriteriaType(req.getCriteriaType());
        achievement.setCriteriaValue(req.getCriteriaValue());
        achievement.setEnabled(req.getEnabled());
        Achievement saved = achievementRepo.save(achievement);
        return AdminAchievementResponse.from(saved, userAchievementRepo.countByAchievementId(achievementId));
    }

    /** Hard-deletes an achievement no one has earned yet. If at least one
     *  user already unlocked it, disables it instead so their badge stays
     *  intact — it just stops being obtainable by anyone else. */
    @Transactional
    public AchievementDeletionResult deleteAchievement(Long achievementId) {
        Achievement achievement = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));

        if (userAchievementRepo.existsByAchievementId(achievementId)) {
            achievement.setEnabled(false);
            Achievement saved = achievementRepo.save(achievement);
            long count = userAchievementRepo.countByAchievementId(achievementId);
            return AchievementDeletionResult.builder()
                    .deleted(false)
                    .message("Users have already earned this achievement, so it was disabled " +
                            "instead of deleted — it's now hidden from anyone who hasn't unlocked it.")
                    .achievement(AdminAchievementResponse.from(saved, count))
                    .build();
        }

        achievementRepo.deleteById(achievementId);
        return AchievementDeletionResult.builder()
                .deleted(true)
                .message("Achievement deleted.")
                .achievement(null)
                .build();
    }

    private Set<Long> getAllPrerequisiteIds(Long skillId) {
        Set<Long> visited = new java.util.LinkedHashSet<>();
        java.util.Queue<Long> queue = new java.util.LinkedList<>();
        queue.add(skillId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            depRepo.findBySkillId(current).forEach(dep -> {
                if (visited.add(dep.getPrerequisiteId()))
                    queue.add(dep.getPrerequisiteId());
            });
        }
        return visited;
    }
}