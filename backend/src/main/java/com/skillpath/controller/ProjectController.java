package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/projects") @RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;
    private final RecommendationService recommendationService;
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> browse(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) List<Long> roleIds,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectService.search(difficulty, skillIds, roleIds, q, PageRequest.of(page, size))));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getById(id, currentUserId(auth))));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(Authentication auth, @Valid @RequestBody CreateProjectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.create(currentUserId(auth),req)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable Long id, Authentication auth, @Valid @RequestBody UpdateProjectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.update(id, currentUserId(auth), req)));
    }
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> join(@PathVariable Long id, Authentication auth) {
        Long userId = currentUserId(auth);
        projectService.requestJoin(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateMember(@PathVariable Long id, @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberStatusRequest req, Authentication auth) {
        projectService.updateMemberStatus(id, currentUserId(auth), userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> members(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getMembers(id, currentUserId(auth))));
    }
    @GetMapping("/{id}/team")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> team(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getTeam(id, currentUserId(auth))));
    }
    @PostMapping("/{id}/invite/{userId}")
    public ResponseEntity<ApiResponse<Void>> invite(@PathVariable Long id, @PathVariable Long userId, Authentication auth) {
        projectService.inviteMember(id, currentUserId(auth), userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long userId, Authentication auth) {
        projectService.removeMember(id, currentUserId(auth), userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @GetMapping("/{id}/recommended-members")
    public ResponseEntity<ApiResponse<List<MatchScoreResponse>>> recommended(
            @PathVariable Long id, @RequestParam(defaultValue = "5") int topN, Authentication auth) {
        projectService.assertOwner(id, currentUserId(auth));
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.recommendTeammates(id, topN)));
    }
    private Long currentUserId(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return userService.getEntityByFirebaseUid(p.getUid()).getId();
    }
}