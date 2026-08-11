package com.skillpath.model.UserAchievement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/** Records that a user has unlocked a given achievement, and when. */
@Entity @Table(name = "user_achievements")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAchievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "achievement_id", nullable = false) private Long achievementId;
    @CreationTimestamp @Column(name = "unlocked_at", updatable = false) private Instant unlockedAt;
}