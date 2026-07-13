package com.skillpath.model.RoadmapStep;
import com.skillpath.model.enums.StepStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "roadmap_steps")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoadmapStep {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "user_id", nullable = false) private Long userId;
 @Column(name = "skill_id") private Long skillId;
 @Column(name = "step_order", nullable = false) private int stepOrder;
 @Enumerated(EnumType.STRING) @Builder.Default
 private StepStatus status = StepStatus.PENDING;
 @Column(name = "completed_at") private Instant completedAt;
}
