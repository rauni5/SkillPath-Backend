package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.CreateCommentRequest;
import com.skillpath.dto.request.CreatePostRequest;
import com.skillpath.dto.request.UpdateCommentRequest;
import com.skillpath.dto.request.UpdatePostRequest;
import com.skillpath.dto.response.ProjectCommentResponse;
import com.skillpath.dto.response.ProjectPostResponse;
import com.skillpath.dto.response.ToggleLikeResponse;
import com.skillpath.model.enums.DiscussionChannel;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.ProjectDiscussionService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/projects/{projectId}/discussion") @RequiredArgsConstructor
public class ProjectDiscussionController {
    private final ProjectDiscussionService discussionService;
    private final UserService userService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<ProjectPostResponse>>> listPosts(
            @PathVariable Long projectId,
            @RequestParam DiscussionChannel channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(discussionService.listPosts(
                projectId, channel, currentUserId(auth), PageRequest.of(page, size))));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<ProjectPostResponse>> createPost(
            @PathVariable Long projectId, @Valid @RequestBody CreatePostRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.createPost(projectId, currentUserId(auth), req)));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<ProjectPostResponse>> getPost(
            @PathVariable Long projectId, @PathVariable Long postId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(discussionService.getPost(postId, currentUserId(auth))));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<ProjectPostResponse>> updatePost(
            @PathVariable Long projectId, @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.updatePost(postId, currentUserId(auth), req)));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long projectId, @PathVariable Long postId, Authentication auth) {
        discussionService.deletePost(postId, currentUserId(auth));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<ToggleLikeResponse>> togglePostLike(
            @PathVariable Long projectId, @PathVariable Long postId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.togglePostLike(postId, currentUserId(auth))));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<ProjectCommentResponse>>> listComments(
            @PathVariable Long projectId, @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(discussionService.listComments(
                postId, currentUserId(auth), PageRequest.of(page, size))));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<ProjectCommentResponse>> addComment(
            @PathVariable Long projectId, @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.addComment(postId, currentUserId(auth), req)));
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<ProjectCommentResponse>> updateComment(
            @PathVariable Long projectId, @PathVariable Long postId, @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.updateComment(commentId, currentUserId(auth), req)));
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long projectId, @PathVariable Long postId, @PathVariable Long commentId,
            Authentication auth) {
        discussionService.deleteComment(commentId, currentUserId(auth));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<ToggleLikeResponse>> toggleCommentLike(
            @PathVariable Long projectId, @PathVariable Long postId, @PathVariable Long commentId,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                discussionService.toggleCommentLike(commentId, currentUserId(auth))));
    }

    private Long currentUserId(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return userService.getEntityByFirebaseUid(p.getUid()).getId();
    }
}
