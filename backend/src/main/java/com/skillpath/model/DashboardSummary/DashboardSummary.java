package com.skillpath.model.DashboardSummary;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "dashboard_summaries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardSummary {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}