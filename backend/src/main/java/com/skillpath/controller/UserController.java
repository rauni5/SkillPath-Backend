package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.AddCertificationRequest;
import com.skillpath.dto.request.AddPortfolioItemRequest;
import com.skillpath.dto.request.RegisterDeviceTokenRequest;
import com.skillpath.dto.request.UpdateMemberStatusRequest;
import com.skillpath.dto.request.UpdateProfileRequest;
import com.skillpath.dto.response.CertificationResponse;
import com.skillpath.dto.response.MembershipStatusResponse;
import com.skillpath.dto.response.PortfolioItemResponse;
import com.skillpath.dto.response.PortfolioResponse;
import com.skillpath.dto.response.ProjectInviteResponse;
import com.skillpath.dto.response.ProjectResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.dto.response.UserSearchResultResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.PortfolioService;
import com.skillpath.service.ProjectService;
import com.skillpath.service.UserService;
import com.skillpath.storage.SupabaseStorageClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;
    private final PortfolioService portfolioService;
    private final SupabaseStorageClient storageClient;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }
    @GetMapping("/{id}/portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> portfolio(@PathVariable Long id, Authentication auth) {
        Long viewerId = resolveCallerId(auth);
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.getSummary(id, viewerId)));
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserSearchResultResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(ApiResponse.ok(Page.empty(PageRequest.of(page, size))));
        }
        return ResponseEntity.ok(ApiResponse.ok(userService.search(q, PageRequest.of(page, size))));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateProfileRequest req, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id,req)));
    }
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        requireSelf(id, auth);

        if (file.isEmpty()) throw new IllegalArgumentException("No image provided.");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Image must be under 5MB.");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new IllegalArgumentException("File must be an image.");

        String extension = "image/png".equals(contentType) ? "png" : "jpg";
        String path = "user-" + id + "." + extension;
        String url = storageClient.uploadAvatar(path, file.getBytes(), contentType);

        UpdateProfileRequest avatarUpdate = new UpdateProfileRequest();
        avatarUpdate.setAvatarUrl(url);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id, avatarUpdate)));
    }
    @PostMapping("/{id}/portfolio")
    public ResponseEntity<ApiResponse<PortfolioItemResponse>> addPortfolioItem(
            @PathVariable Long id, @Valid @RequestBody AddPortfolioItemRequest req, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.addItem(id, req)));
    }
    @DeleteMapping("/{id}/portfolio/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolioItem(
            @PathVariable Long id, @PathVariable Long itemId, Authentication auth) {
        requireSelf(id, auth);
        portfolioService.deleteItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @PostMapping("/{id}/certifications")
    public ResponseEntity<ApiResponse<CertificationResponse>> addCertification(
            @PathVariable Long id, @Valid @RequestBody AddCertificationRequest req, Authentication auth) {
        requireSelf(id, auth);
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.addCertification(id, req)));
    }
    @DeleteMapping("/{id}/certifications/{certId}")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(
            @PathVariable Long id, @PathVariable Long certId, Authentication auth) {
        requireSelf(id, auth);
        portfolioService.deleteCertification(id, certId);
        return ResponseEntity.ok(ApiResponse.ok(null));
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
    private Long resolveCallerId(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return userService.getEntityByFirebaseUid(p.getUid()).getId();
    }
}