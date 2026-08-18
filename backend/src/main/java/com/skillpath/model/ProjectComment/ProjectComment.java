package com.skillpath.model.ProjectComment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity @Table(name = "project_comments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "post_id", nullable = false) private Long postId;
    @Column(name = "author_id", nullable = false) private Long authorId;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(name = "like_count", nullable = false) @Builder.Default private int likeCount = 0;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private Instant updatedAt;
}