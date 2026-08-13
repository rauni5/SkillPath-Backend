package com.skillpath.service;

import com.skillpath.dto.request.*;
import com.skillpath.dto.response.BranchRequirementResponse;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkill;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkillId;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleBranch.RoleBranch;
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
    private final RoleBranchRepository        branchRepo;
    private final BranchRequiredSkillRepository branchSkillRepo;
    private final UserRepository              userRepo;
    private final SkillTrieService            trieService;

    // SKILL MANAGEMENT
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
                "Can't delete this skill — it's still required by at least one project or branch. " +
                "Remove it from those first.");
        }
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

    // CAREER ROLE MANAGEMENT
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

    // BRANCH MANAGEMENT — every role's actual required skills now live
    // exclusively on its branches, not on the role directly.
    @Transactional
    public RoleBranch createBranch(Long roleId, CreateBranchRequest req) {
        if (!roleRepo.existsById(roleId))
            throw new ResourceNotFoundException("Career role not found: " + roleId);
        boolean nameTaken = branchRepo.findByRoleId(roleId).stream()
                .anyMatch(b -> b.getName().equalsIgnoreCase(req.getName()));
        if (nameTaken)
            throw new IllegalArgumentException(
                "A branch named '" + req.getName() + "' already exists for this role.");
        RoleBranch branch = RoleBranch.builder()
                .roleId(roleId)
                .name(req.getName())
                .description(req.getDescription())
                .build();
        return branchRepo.save(branch);
    }

    public List<RoleBranch> getBranches(Long roleId) {
        if (!roleRepo.existsById(roleId))
            throw new ResourceNotFoundException("Career role not found: " + roleId);
        return branchRepo.findByRoleId(roleId);
    }

    public RoleBranch getBranch(Long branchId) {
        return branchRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));
    }

    @Transactional
    public RoleBranch updateBranch(Long branchId, CreateBranchRequest req) {
        RoleBranch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));
        boolean nameTaken = branchRepo.findByRoleId(branch.getRoleId()).stream()
                .anyMatch(b -> !b.getId().equals(branchId) && b.getName().equalsIgnoreCase(req.getName()));
        if (nameTaken)
            throw new IllegalArgumentException(
                "A branch named '" + req.getName() + "' already exists for this role.");
        branch.setName(req.getName());
        branch.setDescription(req.getDescription());
        return branchRepo.save(branch);
    }

    @Transactional
    public void deleteBranch(Long branchId) {
        if (!branchRepo.existsById(branchId))
            throw new ResourceNotFoundException("Branch not found: " + branchId);
        try {
            branchRepo.deleteById(branchId);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Can't delete this branch — at least one user currently has it selected as " +
                "their career goal. That must change first.");
        }
    }

    @Transactional
    public void addBranchRequirement(Long branchId, AddRequirementRequest req) {
        if (!branchRepo.existsById(branchId))
            throw new ResourceNotFoundException("Branch not found: " + branchId);
        if (!skillRepo.existsById(req.getSkillId()))
            throw new ResourceNotFoundException("Skill not found: " + req.getSkillId());

        boolean exists = branchSkillRepo.findByBranchId(branchId).stream()
                .anyMatch(r -> r.getSkillId().equals(req.getSkillId()));
        if (exists)
            throw new IllegalArgumentException(
                "This skill is already a requirement for this branch.");

        branchSkillRepo.save(BranchRequiredSkill.builder()
                .branchId(branchId)
                .skillId(req.getSkillId())
                .importance(req.getImportance())
                .build());
    }

    @Transactional
    public void updateBranchRequirement(Long branchId, Long skillId, AddRequirementRequest req) {
        BranchRequiredSkillId id = new BranchRequiredSkillId(branchId, skillId);
        BranchRequiredSkill existing = branchSkillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Requirement not found for branch " + branchId + " / skill " + skillId));
        existing.setImportance(req.getImportance());
        branchSkillRepo.save(existing);
    }

    @Transactional
    public void removeBranchRequirement(Long branchId, Long skillId) {
        BranchRequiredSkillId id = new BranchRequiredSkillId(branchId, skillId);
        if (!branchSkillRepo.existsById(id))
            throw new ResourceNotFoundException(
                "Requirement not found for branch " + branchId + " / skill " + skillId);
        branchSkillRepo.deleteById(id);
    }

    public List<BranchRequirementResponse> getBranchRequirements(Long branchId) {
        if (!branchRepo.existsById(branchId))
            throw new ResourceNotFoundException("Branch not found: " + branchId);
        return branchSkillRepo.findByBranchId(branchId).stream()
                .map(r -> {
                    Skill skill = skillRepo.findById(r.getSkillId()).orElseThrow();
                    return BranchRequirementResponse.builder()
                            .skillId(skill.getId())
                            .name(skill.getName())
                            .category(skill.getCategory().name())
                            .importance(r.getImportance())
                            .build();
                })
                .toList();
    }

    // USER MANAGEMENT
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