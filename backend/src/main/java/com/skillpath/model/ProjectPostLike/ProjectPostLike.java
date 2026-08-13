package com.skillpath.model.ProjectPostLike;
import jakarta.persistence.*;
import lombok.*;

/** One like per (post, user) — presence of a row means "liked". */
@Entity @Table(name = "project_post_likes")
@IdClass(ProjectPostLikeId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectPostLike {
    @Id @Column(name = "post_id") private Long postId;
    @Id @Column(name = "user_id") private Long userId;
}