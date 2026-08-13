package com.skillpath.model.ProjectPost;
import com.skillpath.model.enums.DiscussionChannel;
import com.skillpath.model.enums.PostTag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity @Table(name = "project_posts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "project_id", nullable = false) private Long projectId;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private DiscussionChannel channel;

    @Column(name = "author_id", nullable = false) private Long authorId;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default
    private PostTag tag = PostTag.GENERAL;

    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;

    @Column(name = "like_count", nullable = false) @Builder.Default private int likeCount = 0;
    @Column(name = "comment_count", nullable = false) @Builder.Default private int commentCount = 0;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private Instant updatedAt;
}