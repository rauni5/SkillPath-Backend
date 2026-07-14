package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/projects") @RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> browse(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.browseOpen(PageRequest.of(page, size))));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getById(id)));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(Authentication auth, @Valid @RequestBody CreateProjectRequest req) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long userId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        return ResponseEntity.ok(ApiResponse.ok(projectService.create(userId,req)));
    }
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> join(@PathVariable Long id, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long userId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        projectService.requestJoin(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateMember(@PathVariable Long id, @PathVariable Long userId,@Valid @RequestBody UpdateMemberStatusRequest req) {
        projectService.updateMemberStatus(id, userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}