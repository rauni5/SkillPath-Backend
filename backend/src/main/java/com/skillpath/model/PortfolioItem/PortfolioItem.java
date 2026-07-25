package com.skillpath.model.PortfolioItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "portfolio_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PortfolioItem {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "user_id", nullable = false) private Long userId;
 @Column(name = "project_id") private Long projectId;
 @Column(name = "github_url", length = 500) private String githubUrl;
 @Column(columnDefinition = "TEXT") private String description;
 @Column(name = "user_role", length = 100) private String userRole;
 @CreationTimestamp @Column(name = "created_at", updatable = false)
 private Instant createdAt;
}
