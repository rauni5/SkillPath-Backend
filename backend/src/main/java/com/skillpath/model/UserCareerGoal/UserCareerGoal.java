package com.skillpath.model.UserCareerGoal;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "user_career_goals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserCareerGoal {
 @Id @Column(name = "user_id") 
 private Long userId;
 @Column(name = "role_id") 
 private Long roleId;
 /** Every role now requires a branch to be selectable — null only means
  * "not yet resolved," which setGoal always fills in before saving. */
 @Column(name = "branch_id")
 private Long branchId;
 @Column(name = "set_at") @Builder.Default
 private Instant setAt = Instant.now();
}
