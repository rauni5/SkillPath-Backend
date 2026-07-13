package com.skillpath.model.UserCareerGoal;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "user_career_goals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserCareerGoal {
 @Id @Column(name = "user_id") private Long userId;
 @Column(name = "role_id") private Long roleId;
 @Column(name = "set_at") final private Instant setAt = Instant.now();
}
