package com.skillpath.model.SkillCheckAttempt;
import com.skillpath.model.enums.Proficiency;
import com.skillpath.model.enums.SkillCheckStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "skill_check_attempts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillCheckAttempt {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "user_id", nullable = false) private Long userId;
 @Column(name = "skill_id", nullable = false) private Long skillId;
 /** Full quiz including correct answers, stored server-side only. */
 @Column(name = "questions_json", nullable = false, columnDefinition = "TEXT") private String questionsJson;
 @Enumerated(EnumType.STRING) @Builder.Default
 private SkillCheckStatus status = SkillCheckStatus.GENERATED;
 private Integer score;
 @Enumerated(EnumType.STRING) private Proficiency proficiency;
 @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
 @Column(name = "submitted_at") private Instant submittedAt;
}