package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.SetCareerGoalRequest;
import com.skillpath.dto.request.SwitchBranchRequest;
import com.skillpath.dto.response.BranchRecommendationResponse;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.dto.response.RoleBranchResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.RoleBranch.RoleBranch;
import com.skillpath.repository.CareerRoleRepository;
import com.skillpath.repository.RoleBranchRepository;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.CareerGoalService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
public class CareerGoalController {
    private final CareerGoalService goalService;
    private final CareerRoleRepository roleRepo;
    private final RoleBranchRepository branchRepo;
    private final UserService userService;

    @GetMapping("/api/v1/career-roles")
    public ResponseEntity<ApiResponse<List<CareerRole>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleRepo.findAll()));
    }

    @GetMapping("/api/v1/career-roles/{roleId}/branches")
    public ResponseEntity<ApiResponse<List<RoleBranchResponse>>> listBranches(@PathVariable Long roleId) {
        List<RoleBranchResponse> branches = branchRepo.findByRoleId(roleId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @PostMapping("/api/v1/users/{userId}/career-goal")
    public ResponseEntity<ApiResponse<Void>> setGoal(
            @PathVariable Long userId, @Valid @RequestBody SetCareerGoalRequest req, Authentication auth) {
        requireSelf(userId, auth);
        goalService.setGoal(userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/api/v1/users/{userId}/career-goal/branch")
    public ResponseEntity<ApiResponse<Void>> switchBranch(
            @PathVariable Long userId, @Valid @RequestBody SwitchBranchRequest req, Authentication auth) {
        requireSelf(userId, auth);
        goalService.switchBranch(userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/api/v1/users/{userId}/career-goal/branch-recommendations")
    public ResponseEntity<ApiResponse<List<BranchRecommendationResponse>>> branchRecommendations(
            @PathVariable Long userId, @RequestParam Long roleId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(goalService.recommendBranches(userId, roleId)));
    }

    @GetMapping("/api/v1/users/{userId}/career-goal/gap")
    public ResponseEntity<ApiResponse<GapAnalysisResponse>> gap(@PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(goalService.getGapAnalysis(userId)));
    }

    private RoleBranchResponse toResponse(RoleBranch b) {
        return RoleBranchResponse.builder()
                .id(b.getId()).roleId(b.getRoleId())
                .name(b.getName()).description(b.getDescription())
                .build();
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own career goal data.");
    }
}