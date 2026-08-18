package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;

@Getter @Builder
public class ProjectCommentResponse {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String body;
    private int likeCount;
    private boolean likedByMe;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean canEdit;
    private boolean canDelete;
}