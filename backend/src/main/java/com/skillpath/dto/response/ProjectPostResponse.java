package com.skillpath.dto.response;
import com.skillpath.model.enums.DiscussionChannel;
import com.skillpath.model.enums.PostTag;
import lombok.*;
import java.time.Instant;

@Getter @Builder
public class ProjectPostResponse {
    private Long id;
    private Long projectId;
    private DiscussionChannel channel;
    private PostTag tag;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String title;
    private String body;
    private int likeCount;
    private boolean likedByMe;
    private int commentCount;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean canEdit;
    private boolean canDelete;
}