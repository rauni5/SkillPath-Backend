package com.skillpath.model.UserStreak;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/** Persisted streak record for a user — recomputed from real activity
 *  (roadmap step completions, passed skill checks) each time it's read,
 *  so the longest streak ever reached is never lost. */
@Entity @Table(name = "user_streaks")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserStreak {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "current_streak", nullable = false) @Builder.Default
    private int currentStreak = 0;
    @Column(name = "longest_streak", nullable = false) @Builder.Default
    private int longestStreak = 0;
    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;
}