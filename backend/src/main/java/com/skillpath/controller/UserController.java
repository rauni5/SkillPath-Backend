package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.RegisterDeviceTokenRequest;
import com.skillpath.dto.request.UpdateMemberStatusRequest;
import com.skillpath.dto.request.UpdateProfileRequest;
import com.skillpath.dto.response.MembershipStatusResponse;
import com.skillpath.dto.response.ProjectInviteResponse;
import com.skillpath.dto.response.ProjectResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.ProjectService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id,req)));
    }
    @GetMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> ownedProjects(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getOwnedProjects(id, PageRequest.of(page, size))));
    }
    @GetMapping("/{id}/invites")
    public ResponseEntity<ApiResponse<List<ProjectInviteResponse>>> invites(@PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(projectService.getMyInvites(id)));
    }
    @PatchMapping("/{id}/invites/{projectId}")
    public ResponseEntity<ApiResponse<Void>> respondToInvite(
            @PathVariable Long id, @PathVariable Long projectId,
            @Valid @RequestBody UpdateMemberStatusRequest req, Authentication auth) {
        requireSelf(id, auth);
        projectService.respondToInvite(id, projectId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @GetMapping("/{id}/memberships")
    public ResponseEntity<ApiResponse<List<MembershipStatusResponse>>> memberships(@PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(projectService.getMyMemberships(id)));
    }
    @PutMapping("/{id}/device-token")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @PathVariable Long id, @Valid @RequestBody RegisterDeviceTokenRequest req, Authentication auth) {
        requireSelf(id, auth);
        userService.registerDeviceToken(id, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @DeleteMapping("/{id}/device-token")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @PathVariable Long id, @RequestParam String token, Authentication auth) {
        requireSelf(id, auth);
        userService.unregisterDeviceToken(token);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own data.");
    }
}