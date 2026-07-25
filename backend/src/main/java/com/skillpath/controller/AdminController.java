package com.skillpath.controller;

import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // one-time bootstrap

    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<UserResponse>> setup(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.bootstrapFirstAdmin(p.getUid())));
    }

    // SKILL MANAGEMENT
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(
            @Valid @RequestBody CreateSkillRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createSkill(req)));
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
    @PostMapping("/career-roles")
    public ResponseEntity<ApiResponse<CareerRole>> createCareerRole(
            @Valid @RequestBody CreateCareerRoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createCareerRole(req)));
    }

    @PostMapping("/career-roles/{roleId}/requirements")
    public ResponseEntity<ApiResponse<Void>> addRequirement(
            @PathVariable Long roleId,
            @Valid @RequestBody AddRequirementRequest req) {
        adminService.addRoleRequirement(roleId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/career-roles/{roleId}/requirements/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeRequirement(
            @PathVariable Long roleId,
            @PathVariable Long skillId) {
        adminService.removeRoleRequirement(roleId, skillId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }


    @GetMapping("/career-roles/{roleId}/requirements")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getRequirements(
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getRoleRequirements(roleId)));
    }

    // USER MANAGEMENT
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listAllUsers()));
    }

    @PatchMapping("/users/{userId}/admin")
    public ResponseEntity<ApiResponse<UserResponse>> setAdmin(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, Boolean> body) {
        boolean isAdmin = Boolean.TRUE.equals(body.get("admin"));
        return ResponseEntity.ok(ApiResponse.ok(adminService.setAdminFlag(userId, isAdmin)));
    }
}