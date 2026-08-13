package com.skillpath.model.ProjectCommentLike;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "project_comment_likes")
@IdClass(ProjectCommentLikeId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectCommentLike {
    @Id @Column(name = "comment_id") private Long commentId;
    @Id @Column(name = "user_id") private Long userId;
}