package com.skillpath.controller;

import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.PublicProfileResponse;
import com.skillpath.dto.response.PublicProfileSettingsResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.PublicProfileService;
import com.skillpath.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PublicProfileController {

    private final PublicProfileService publicProfileService;
    private final UserService userService;

    // --- Authenticated: managing your own share settings ---

    @GetMapping("/api/v1/users/{id}/public-profile")
    public ResponseEntity<ApiResponse<PublicProfileSettingsResponse>> getSettings(
            @PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(publicProfileService.getSettings(id)));
    }

    @PostMapping("/api/v1/users/{id}/public-profile/enable")
    public ResponseEntity<ApiResponse<PublicProfileSettingsResponse>> enable(
            @PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(publicProfileService.enable(id)));
    }

    @PostMapping("/api/v1/users/{id}/public-profile/disable")
    public ResponseEntity<ApiResponse<PublicProfileSettingsResponse>> disable(
            @PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(publicProfileService.disable(id)));
    }

    @PostMapping("/api/v1/users/{id}/public-profile/regenerate")
    public ResponseEntity<ApiResponse<PublicProfileSettingsResponse>> regenerate(
            @PathVariable Long id, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(publicProfileService.regenerate(id)));
    }

    // --- Public: no Authentication param here on purpose. This path is
    // whitelisted in SecurityConfig, so FirebaseTokenFilter never rejects
    // a request here for lacking a token — this must stay reachable by a
    // visitor with no SkillPath account at all. ---

    @GetMapping("/api/v1/public/profiles/{token}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(
            @PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(publicProfileService.getPublicProfile(token)));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own data.");
    }
}
