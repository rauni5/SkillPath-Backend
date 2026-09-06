package com.skillpath.service;

import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkill;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkillId;
import com.skillpath.model.Achievement.Achievement;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleBranch.RoleBranch;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.SkillDependency.SkillDependency;
import com.skillpath.model.SkillDependency.SkillDependencyId;
import com.skillpath.model.User.User;
import com.skillpath.model.enums.ProjectStatus;
import com.skillpath.model.enums.Proficiency;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final AchievementRepository       achievementRepo;
    private final UserAchievementRepository   userAchievementRepo;
    private final ProjectRepository           projectRepo;
    private final UserSkillRepository         userSkillRepo;
    private final UserCareerGoalRepository    userCareerGoalRepo;
    private final BranchRequiredSkillRepository branchRequiredSkillRepo;
    // NOTE: FirebaseAnalyticsService is intentionally no longer wired in —
    // the GA4 numbers weren't reporting anything useful in practice, so the
    // dashboard stopped calling it. The service class is still here if you
    // want to bring it back later; just re-add the field + call below.

    // SKILL MANAGEMENT
    @Transactional
    public SkillResponse createSkill(CreateSkillRequest req) {
        if (skillRepo.existsByNameIgnoreCase(req.getName()))
            throw new IllegalArgumentException(
                "A skill named '" + req.getName() + "' already exists.");

        Skill skill = Skill.builder()
                .name(req.getName())
                .category(Skill.normalizeCategory(req.getCategory()))
                .description(req.getDescription())
                .build();
        Skill saved = skillRepo.save(skill);

        // Keep the live Trie in sync — no restart required
        trieService.insertSkill(saved.getName(), saved.getId());

        return SkillResponse.from(saved);
    }

    /** All categories currently used by at least one skill, for the admin
     *  UI's category autocomplete — never a fixed/hardcoded list. */
    public List<String> listSkillCategories() {
        return skillRepo.findDistinctCategories();
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
        skill.setCategory(Skill.normalizeCategory(req.getCategory()));
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

    /**
     * All career roles enriched with stats (# of unique required skills across all branches, 
     * # of users who chose it as their goal) — powers the admin Roles list.
     */
    public List<AdminRoleSummaryResponse> listRolesWithStats() {
        List<CareerRole> roles = roleRepo.findAll();

        // Map of roleId -> count of distinct required skills across all branches of that role
        Map<Long, Long> requirementCounts = branchRequiredSkillRepo.countDistinctSkillsGroupedByRole().stream()
                .collect(Collectors.toMap(
                        BranchRequiredSkillRepository.CountByRole::getRoleId,
                        BranchRequiredSkillRepository.CountByRole::getCnt));

        Map<Long, Long> popularity = userCareerGoalRepo.countGroupedByRole().stream()
                .collect(Collectors.toMap(
                        UserCareerGoalRepository.CountByRole::getRoleId,
                        UserCareerGoalRepository.CountByRole::getCnt));

        return roles.stream()
                .map(r -> AdminRoleSummaryResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .requirementsCount(requirementCounts.getOrDefault(r.getId(), 0L))
                        .popularity(popularity.getOrDefault(r.getId(), 0L))
                        .build())
                .toList();
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
                            .category(skill.getCategory())
                            .importance(r.getImportance())
                            .build();
                })
                .toList();
    }

    /** Paginated, optionally filtered by a name/email search term. Each
     *  row is enriched with per-user stats (skills/projects/achievements
     *  counts, career-goal flag), batched into one query per stat rather
     *  than N+1 per page. */
    /** Paginated, optionally filtered by a name/email search term and a
     *  status filter (all / admins only / active only / inactive only).
     *  Each row is enriched with per-user stats (skills/projects/
     *  achievements counts, career-goal flag), batched into one query per
     *  stat rather than N+1 per page. */
    public Page<AdminUserSummaryResponse> listUsers(String q, String status, Pageable pageable) {
        String normalizedQ = (q == null || q.isBlank()) ? null : q.trim();
        Boolean adminFilter = "ADMIN".equalsIgnoreCase(status) ? Boolean.TRUE : null;
        Boolean activeFilter = switch (status == null ? "ALL" : status.toUpperCase()) {
            case "ACTIVE" -> Boolean.TRUE;
            case "INACTIVE" -> Boolean.FALSE;
            default -> null;
        };
        Page<User> page = userRepo.search(normalizedQ, adminFilter, activeFilter, pageable);
        List<Long> ids = page.getContent().stream().map(User::getId).toList();

        Map<Long, Long> skillCounts = ids.isEmpty() ? Map.of() : userSkillRepo.countByUserIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(UserSkillRepository.CountByUser::getUserId, UserSkillRepository.CountByUser::getCnt));
        Map<Long, Long> projectCounts = ids.isEmpty() ? Map.of() : projectRepo.countByOwnerIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(ProjectRepository.CountByOwner::getOwnerId, ProjectRepository.CountByOwner::getCnt));
        Map<Long, Long> achievementCounts = ids.isEmpty() ? Map.of() : userAchievementRepo.countByUserIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(UserAchievementRepository.CountByUser::getUserId, UserAchievementRepository.CountByUser::getCnt));
        Set<Long> goalSetIds = ids.isEmpty() ? Set.of() : Set.copyOf(userCareerGoalRepo.findUserIdsWithGoalSet(ids));

        return page.map(u -> AdminUserSummaryResponse.builder()
                .user(UserResponse.from(u))
                .skillsCount(skillCounts.getOrDefault(u.getId(), 0L))
                .ownedProjectsCount(projectCounts.getOrDefault(u.getId(), 0L))
                .achievementsCount(achievementCounts.getOrDefault(u.getId(), 0L))
                .careerGoalSet(goalSetIds.contains(u.getId()))
                .build());
    }

    /** Aggregate stats for the admin "Users" screen. */
    public AdminUserAnalyticsResponse getUserAnalytics() {
        long totalUsers = userRepo.count();
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        Map<String, Long> byExperience = new LinkedHashMap<>();
        for (Proficiency level : Proficiency.values()) {
            byExperience.put(level.name(), userRepo.countByExperienceLevel(level));
        }

        long usersWithSkills = userSkillRepo.countDistinctUsers();
        double avgSkillsPerUser = usersWithSkills == 0
                ? 0.0
                : (double) totalSkillOwnershipRows() / usersWithSkills;

        List<DailyCountResponse> trend = signupTrend(thirtyDaysAgo);

        return AdminUserAnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .adminCount(userRepo.countByAdminTrue())
                .availableCount(userRepo.countByAvailabilityTrue())
                .unavailableCount(totalUsers - userRepo.countByAvailabilityTrue())
                .usersWithCareerGoalSet(userCareerGoalRepo.count())
                .avgSkillsPerUser(Math.round(avgSkillsPerUser * 100.0) / 100.0)
                .newUsersLast7Days(userRepo.countByCreatedAtAfter(sevenDaysAgo))
                .newUsersLast30Days(userRepo.countByCreatedAtAfter(thirtyDaysAgo))
                .byExperienceLevel(byExperience)
                .signupTrend(trend)
                .build();
    }

    /** Platform-wide stats for the admin overview/dashboard screen.
     *  [trendDays] controls the signup-trend window (7/30/90 etc); other
     *  stats are always as-of-now regardless of that window. */
    public AdminDashboardStatsResponse getDashboardStats(int trendDays) {
        long totalUsers = userRepo.count();
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        Instant trendSince = now.minus(trendDays, ChronoUnit.DAYS);

        long usersWithSkills = userSkillRepo.countDistinctUsers();
        double avgSkillsPerUser = usersWithSkills == 0
                ? 0.0
                : (double) totalSkillOwnershipRows() / usersWithSkills;

        List<SkillPopularityResponse> topSkills = userSkillRepo
                .findTopSkills(org.springframework.data.domain.PageRequest.of(0, 5))
                .stream()
                .map(sc -> skillRepo.findById(sc.getSkillId())
                        .map(skill -> SkillPopularityResponse.builder()
                                .skillId(skill.getId())
                                .name(skill.getName())
                                .userCount(sc.getCnt())
                                .build())
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .newUsersLast7Days(userRepo.countByCreatedAtAfter(sevenDaysAgo))
                .newUsersLast30Days(userRepo.countByCreatedAtAfter(thirtyDaysAgo))
                .totalProjects(projectRepo.count())
                .openProjects(projectRepo.countByStatus(ProjectStatus.OPEN))
                .completedProjects(projectRepo.countByStatus(ProjectStatus.COMPLETED))
                .totalSkills(skillRepo.count())
                .totalCareerRoles(roleRepo.count())
                .totalAchievements(achievementRepo.count())
                .achievementsUnlockedCount(userAchievementRepo.count())
                .avgSkillsPerUser(Math.round(avgSkillsPerUser * 100.0) / 100.0)
                .topSkills(topSkills)
                .userSignupTrend(signupTrend(trendSince))
                .recentSignups(getRecentSignups(5))
                .build();
    }

    /** Just the signup-trend series, for the dashboard's day-range switcher
     *  (7d/30d/90d) to refresh without re-fetching the whole dashboard. */
    public List<DailyCountResponse> getSignupTrend(int days) {
        return signupTrend(Instant.now().minus(days, ChronoUnit.DAYS));
    }

    /** The most recently joined users, newest first — powers the "Recent
     *  Signups" panel on the dashboard. */
    public List<RecentUserResponse> getRecentSignups(int limit) {
        return userRepo.findAll(org.springframework.data.domain.PageRequest.of(0, limit,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(u -> RecentUserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .avatarUrl(u.getAvatarUrl())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }

    private List<DailyCountResponse> signupTrend(Instant since) {
        return userRepo.signupTrendSince(since).stream()
                .map(row -> DailyCountResponse.builder()
                        .date(row.date())
                        .count(row.count())
                        .build())
                .toList();
    }

    /** Total rows in user_skills — i.e. sum of skills owned across all users. */
    private long totalSkillOwnershipRows() {
        return userSkillRepo.count();
    }
    // USER MANAGEMENT
    public List<UserResponse> listAllUsers() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setAdminFlag(Long userId, boolean isAdmin, Long requestingUserId) {
        if (!isAdmin && userId.equals(requestingUserId)) {
            throw new IllegalArgumentException("You can't revoke your own admin access.");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found: " + userId));
        user.setAdmin(isAdmin);
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public UserResponse setActiveFlag(Long userId, boolean active, Long requestingUserId) {
        if (!active && userId.equals(requestingUserId)) {
            throw new IllegalArgumentException("You can't deactivate your own account.");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found: " + userId));
        user.setActive(active);
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

    /** Resolves the DB id behind a Firebase UID — used by the controller to
     *  find "who is making this request" for the self-lockout checks above. */
    public Long resolveUserId(String firebaseUid) {
        return userRepo.findByFirebaseUid(firebaseUid).map(User::getId).orElse(null);
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