package com.skillpath.controller;

import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleBranch.RoleBranch;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // SKILL MANAGEMENT
    @GetMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getSkill(skillId)));
    }
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(
            @Valid @RequestBody CreateSkillRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createSkill(req)));
    }
    @PutMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody CreateSkillRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateSkill(skillId, req)));
    }
    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long skillId) {
        adminService.deleteSkill(skillId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/skills/{skillId}/dependencies")
    public ResponseEntity<ApiResponse<Void>> addDependency(
            @PathVariable Long skillId,
            @Valid @RequestBody AddDependencyRequest req) {
        adminService.addDependency(skillId, req.getPrerequisiteId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/skills/{skillId}/dependencies/{prereqId}")
    public ResponseEntity<ApiResponse<Void>> removeDependency(
            @PathVariable Long skillId,
            @PathVariable Long prereqId) {
        adminService.removeDependency(skillId, prereqId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/skills/{skillId}/dependencies")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getDependencies(
            @PathVariable Long skillId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDependencies(skillId)));
    }


    // CAREER ROLE MANAGEMENT
    /** All roles with per-role stats (required-skill count, popularity) —
     *  used by the admin Roles list instead of the plain public list. */
    @GetMapping("/career-roles")
    public ResponseEntity<ApiResponse<List<AdminRoleSummaryResponse>>> listCareerRoles() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listRolesWithStats()));
    }

    @GetMapping("/career-roles/{roleId}")
    public ResponseEntity<ApiResponse<CareerRole>> getCareerRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getCareerRole(roleId)));
    }

    @PostMapping("/career-roles")
    public ResponseEntity<ApiResponse<CareerRole>> createCareerRole(
            @Valid @RequestBody CreateCareerRoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createCareerRole(req)));
    }

    @PutMapping("/career-roles/{roleId}")
    public ResponseEntity<ApiResponse<CareerRole>> updateCareerRole(
            @PathVariable Long roleId,
            @Valid @RequestBody CreateCareerRoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateCareerRole(roleId, req)));
    }

    @DeleteMapping("/career-roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteCareerRole(@PathVariable Long roleId) {
        adminService.deleteCareerRole(roleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // BRANCH MANAGEMENT — a role's actual required skills always live on a
    // branch now; there is no direct role-level skill list anymore.
    @GetMapping("/career-roles/{roleId}/branches")
    public ResponseEntity<ApiResponse<List<RoleBranch>>> getBranches(@PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getBranches(roleId)));
    }

    @GetMapping("/branches/{branchId}")
    public ResponseEntity<ApiResponse<RoleBranch>> getBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getBranch(branchId)));
    }

    @PostMapping("/career-roles/{roleId}/branches")
    public ResponseEntity<ApiResponse<RoleBranch>> createBranch(
            @PathVariable Long roleId, @Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createBranch(roleId, req)));
    }

    @PutMapping("/branches/{branchId}")
    public ResponseEntity<ApiResponse<RoleBranch>> updateBranch(
            @PathVariable Long branchId, @Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateBranch(branchId, req)));
    }

    @DeleteMapping("/branches/{branchId}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long branchId) {
        adminService.deleteBranch(branchId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/branches/{branchId}/requirements")
    public ResponseEntity<ApiResponse<Void>> addBranchRequirement(
            @PathVariable Long branchId, @Valid @RequestBody AddRequirementRequest req) {
        adminService.addBranchRequirement(branchId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/branches/{branchId}/requirements/{skillId}")
    public ResponseEntity<ApiResponse<Void>> updateBranchRequirement(
            @PathVariable Long branchId, @PathVariable Long skillId,
            @Valid @RequestBody AddRequirementRequest req) {
        adminService.updateBranchRequirement(branchId, skillId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/branches/{branchId}/requirements/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeBranchRequirement(
            @PathVariable Long branchId, @PathVariable Long skillId) {
        adminService.removeBranchRequirement(branchId, skillId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/branches/{branchId}/requirements")
    public ResponseEntity<ApiResponse<List<BranchRequirementResponse>>> getBranchRequirements(
            @PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getBranchRequirements(branchId)));
    }

    // USER MANAGEMENT
    /** Paginated + searchable + filterable by status (ALL/ADMIN/ACTIVE/INACTIVE).
     *  Sortable by name, email, or createdAt (default: createdAt desc). */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserSummaryResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Set<String> allowedSort = Set.of("name", "email", "createdAt");
        String safeSortBy = allowedSort.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.listUsers(q, status, PageRequest.of(page, size, Sort.by(direction, safeSortBy)))));
    }

    /** Aggregate stats for the admin Users screen (counts, breakdowns, signup trend). */
    @GetMapping("/users/analytics")
    public ResponseEntity<ApiResponse<AdminUserAnalyticsResponse>> userAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUserAnalytics()));
    }

    @PatchMapping("/users/{userId}/admin")
    public ResponseEntity<ApiResponse<UserResponse>> setAdmin(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, Boolean> body,
            Authentication auth) {
        boolean isAdmin = Boolean.TRUE.equals(body.get("admin"));
        Long requestingUserId = currentUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(adminService.setAdminFlag(userId, isAdmin, requestingUserId)));
    }

    /** Deactivate/reactivate a user. Deactivated users are rejected at
     *  sign-in (see FirebaseTokenFilter) but keep their data. */
    @PatchMapping("/users/{userId}/active")
    public ResponseEntity<ApiResponse<UserResponse>> setActive(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, Boolean> body,
            Authentication auth) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        Long requestingUserId = currentUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(adminService.setActiveFlag(userId, active, requestingUserId)));
    }

    private Long currentUserId(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return adminService.resolveUserId(p.getUid());
    }

    // DASHBOARD / OVERVIEW
    /** Platform-wide stats for the admin overview screen.
     *  [days] controls the signup-trend window (defaults to 30). */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> dashboardStats(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats(days)));
    }

    /** Just the signup-trend chart data — lets the dashboard's day-range
     *  switcher (7d/30d/90d) refresh only the chart instead of reloading
     *  every stat on the page. */
    @GetMapping("/dashboard/signup-trend")
    public ResponseEntity<ApiResponse<List<DailyCountResponse>>> signupTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getSignupTrend(days)));
    }

    // ONE-TIME BOOTSTRAP — promotes the calling (already-authenticated) user
    // to admin, but only if no admin exists yet at all.
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<UserResponse>> bootstrap(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(adminService.bootstrapFirstAdmin(p.getUid())));
    }
    // ACHIEVEMENT MANAGEMENT
    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<AdminAchievementResponse>>> listAchievements() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listAchievements()));
    }

    @GetMapping("/achievements/{achievementId}")
    public ResponseEntity<ApiResponse<AdminAchievementResponse>> getAchievement(
            @PathVariable Long achievementId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAchievementAdmin(achievementId)));
    }

    @PostMapping("/achievements")
    public ResponseEntity<ApiResponse<AdminAchievementResponse>> createAchievement(
            @Valid @RequestBody CreateAchievementRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createAchievement(req)));
    }

    @PutMapping("/achievements/{achievementId}")
    public ResponseEntity<ApiResponse<AdminAchievementResponse>> updateAchievement(
            @PathVariable Long achievementId,
            @Valid @RequestBody UpdateAchievementRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateAchievement(achievementId, req)));
    }

    @DeleteMapping("/achievements/{achievementId}")
    public ResponseEntity<ApiResponse<AchievementDeletionResult>> deleteAchievement(
            @PathVariable Long achievementId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.deleteAchievement(achievementId)));
    }
}