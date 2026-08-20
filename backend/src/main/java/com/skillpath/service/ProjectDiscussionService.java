package com.skillpath.service;
import com.skillpath.dto.request.CreateCommentRequest;
import com.skillpath.dto.request.CreatePostRequest;
import com.skillpath.dto.request.UpdateCommentRequest;
import com.skillpath.dto.request.UpdatePostRequest;
import com.skillpath.dto.response.ProjectCommentResponse;
import com.skillpath.dto.response.ProjectPostResponse;
import com.skillpath.dto.response.ToggleLikeResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Project.Project;
import com.skillpath.model.ProjectComment.ProjectComment;
import com.skillpath.model.ProjectCommentLike.ProjectCommentLike;
import com.skillpath.model.ProjectMember.ProjectMemberId;
import com.skillpath.model.ProjectPost.ProjectPost;
import com.skillpath.model.ProjectPostLike.ProjectPostLike;
import com.skillpath.model.User.User;
import com.skillpath.model.enums.DiscussionChannel;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.PostTag;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * Reddit-style discussion boards, two per project: PUBLIC (any
 * authenticated user can read/post/comment/like) and TEAM (owner + accepted
 * members only). All access checks live here so the controller stays a
 * thin pass-through.
 */
@Service @RequiredArgsConstructor
public class ProjectDiscussionService {
    private final ProjectPostRepository postRepo;
    private final ProjectCommentRepository commentRepo;
    private final ProjectPostLikeRepository postLikeRepo;
    private final ProjectCommentLikeRepository commentLikeRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;


    public Page<ProjectPostResponse> listPosts(
            Long projectId, DiscussionChannel channel, Long requesterId, Pageable pageable) {
        Project project = getProject(projectId);
        requireChannelAccess(project, channel, requesterId);
        return postRepo.findByProjectIdAndChannelOrderByCreatedAtDesc(projectId, channel, pageable)
                .map(post -> toPostResponse(post, project, requesterId));
    }

    public ProjectPostResponse getPost(Long postId, Long requesterId) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        requireChannelAccess(project, post.getChannel(), requesterId);
        return toPostResponse(post, project, requesterId);
    }

    @Transactional
    public ProjectPostResponse createPost(Long projectId, Long requesterId, CreatePostRequest req) {
        Project project = getProject(projectId);
        requireChannelAccess(project, req.getChannel(), requesterId);

        ProjectPost post = postRepo.save(ProjectPost.builder()
                .projectId(projectId)
                .channel(req.getChannel())
                .authorId(requesterId)
                .tag(req.getTag() != null ? req.getTag() : PostTag.GENERAL)
                .title(req.getTitle().trim())
                .body(req.getBody().trim())
                .build());
        return toPostResponse(post, project, requesterId);
    }

    @Transactional
    public ProjectPostResponse updatePost(Long postId, Long requesterId, UpdatePostRequest req) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        if (!post.getAuthorId().equals(requesterId))
            throw new ForbiddenException("Only the author can edit this post.");

        post.setTitle(req.getTitle().trim());
        post.setBody(req.getBody().trim());
        if (req.getTag() != null) post.setTag(req.getTag());
        ProjectPost saved = postRepo.save(post);
        return toPostResponse(saved, project, requesterId);
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        requireAuthorOrOwner(post.getAuthorId(), project, requesterId, "post");
        postRepo.delete(post);
    }

    @Transactional
    public ToggleLikeResponse togglePostLike(Long postId, Long requesterId) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        requireChannelAccess(project, post.getChannel(), requesterId);

        boolean alreadyLiked = postLikeRepo.existsByPostIdAndUserId(postId, requesterId);
        if (alreadyLiked) {
            postLikeRepo.deleteByPostIdAndUserId(postId, requesterId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            postLikeRepo.save(ProjectPostLike.builder().postId(postId).userId(requesterId).build());
            post.setLikeCount(post.getLikeCount() + 1);
        }
        postRepo.save(post);
        return ToggleLikeResponse.builder().liked(!alreadyLiked).likeCount(post.getLikeCount()).build();
    }


    public Page<ProjectCommentResponse> listComments(Long postId, Long requesterId, Pageable pageable) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        requireChannelAccess(project, post.getChannel(), requesterId);
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId, pageable)
                .map(c -> toCommentResponse(c, project, requesterId));
    }

    @Transactional
    public ProjectCommentResponse addComment(Long postId, Long requesterId, CreateCommentRequest req) {
        ProjectPost post = getPostOrThrow(postId);
        Project project = getProject(post.getProjectId());
        requireChannelAccess(project, post.getChannel(), requesterId);

        ProjectComment comment = commentRepo.save(ProjectComment.builder()
                .postId(postId)
                .authorId(requesterId)
                .body(req.getBody().trim())
                .build());

        post.setCommentCount(post.getCommentCount() + 1);
        postRepo.save(post);

        if (!post.getAuthorId().equals(requesterId)) {
            User commenter = userRepo.findById(requesterId).orElseThrow();
            notificationService.notifyUser(
                    post.getAuthorId(),
                    "New reply",
                    commenter.getName() + " commented on your post \"" + post.getTitle() + "\"",
                    Map.of("type", NotificationType.DISCUSSION_COMMENT_RECEIVED.getValue(),
                           "projectId", String.valueOf(project.getId()),
                           "postId", String.valueOf(postId)));
        }

        return toCommentResponse(comment, project, requesterId);
    }

    @Transactional
    public ProjectCommentResponse updateComment(Long commentId, Long requesterId, UpdateCommentRequest req) {
        ProjectComment comment = getCommentOrThrow(commentId);
        Project project = getProject(getPostOrThrow(comment.getPostId()).getProjectId());
        if (!comment.getAuthorId().equals(requesterId))
            throw new ForbiddenException("Only the author can edit this comment.");

        comment.setBody(req.getBody().trim());
        ProjectComment saved = commentRepo.save(comment);
        return toCommentResponse(saved, project, requesterId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId) {
        ProjectComment comment = getCommentOrThrow(commentId);
        ProjectPost post = getPostOrThrow(comment.getPostId());
        Project project = getProject(post.getProjectId());
        requireAuthorOrOwner(comment.getAuthorId(), project, requesterId, "comment");

        commentRepo.delete(comment);
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepo.save(post);
    }

    @Transactional
    public ToggleLikeResponse toggleCommentLike(Long commentId, Long requesterId) {
        ProjectComment comment = getCommentOrThrow(commentId);
        ProjectPost post = getPostOrThrow(comment.getPostId());
        Project project = getProject(post.getProjectId());
        requireChannelAccess(project, post.getChannel(), requesterId);

        boolean alreadyLiked = commentLikeRepo.existsByCommentIdAndUserId(commentId, requesterId);
        if (alreadyLiked) {
            commentLikeRepo.deleteByCommentIdAndUserId(commentId, requesterId);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            commentLikeRepo.save(ProjectCommentLike.builder().commentId(commentId).userId(requesterId).build());
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        commentRepo.save(comment);
        return ToggleLikeResponse.builder().liked(!alreadyLiked).likeCount(comment.getLikeCount()).build();
    }


    @Transactional
    public void createAboutPost(Long projectId, Long ownerId, String projectName, String description) {
        postRepo.save(ProjectPost.builder()
                .projectId(projectId)
                .channel(DiscussionChannel.PUBLIC)
                .authorId(ownerId)
                .tag(PostTag.ANNOUNCEMENT)
                .title("About " + projectName)
                .body(description)
                .build());
    }


    private void requireChannelAccess(Project project, DiscussionChannel channel, Long requesterId) {
        if (channel == DiscussionChannel.PUBLIC) return; // any authenticated user
        boolean isOwner = project.getOwnerId().equals(requesterId);
        boolean isAcceptedMember = memberRepo.findById(new ProjectMemberId(project.getId(), requesterId))
                .map(m -> m.getStatus() == MemberStatus.ACCEPTED)
                .orElse(false);
        if (!isOwner && !isAcceptedMember)
            throw new ForbiddenException("Only project members can access the team discussion.");
    }

    private void requireAuthorOrOwner(Long authorId, Project project, Long requesterId, String what) {
        boolean isAuthor = authorId.equals(requesterId);
        boolean isOwner = project.getOwnerId().equals(requesterId);
        if (!isAuthor && !isOwner)
            throw new ForbiddenException("Only the author or the project owner can delete this " + what + ".");
    }

    private Project getProject(Long projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private ProjectPost getPostOrThrow(Long postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
    }

    private ProjectComment getCommentOrThrow(Long commentId) {
        return commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private ProjectPostResponse toPostResponse(ProjectPost post, Project project, Long requesterId) {
        User author = userRepo.findById(post.getAuthorId()).orElse(null);
        boolean likedByMe = postLikeRepo.existsByPostIdAndUserId(post.getId(), requesterId);
        return ProjectPostResponse.builder()
                .id(post.getId())
                .projectId(post.getProjectId())
                .channel(post.getChannel())
                .tag(post.getTag())
                .authorId(post.getAuthorId())
                .authorName(author != null ? author.getName() : "Unknown")
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .title(post.getTitle())
                .body(post.getBody())
                .likeCount(post.getLikeCount())
                .likedByMe(likedByMe)
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .canEdit(post.getAuthorId().equals(requesterId))
                .canDelete(post.getAuthorId().equals(requesterId) || project.getOwnerId().equals(requesterId))
                .build();
    }

    private ProjectCommentResponse toCommentResponse(ProjectComment comment, Project project, Long requesterId) {
        User author = userRepo.findById(comment.getAuthorId()).orElse(null);
        boolean likedByMe = commentLikeRepo.existsByCommentIdAndUserId(comment.getId(), requesterId);
        return ProjectCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(author != null ? author.getName() : "Unknown")
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .body(comment.getBody())
                .likeCount(comment.getLikeCount())
                .likedByMe(likedByMe)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .canEdit(comment.getAuthorId().equals(requesterId))
                .canDelete(comment.getAuthorId().equals(requesterId) || project.getOwnerId().equals(requesterId))
                .build();
    }
}