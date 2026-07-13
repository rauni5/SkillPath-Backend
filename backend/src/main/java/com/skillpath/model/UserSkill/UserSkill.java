package com.skillpath.model.UserSkill;
import com.skillpath.model.enums.Proficiency;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "user_skills")
@IdClass(UserSkillId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSkill {
 @Id @Column(name = "user_id") private Long userId;
 @Id @Column(name = "skill_id") private Long skillId;
 @Enumerated(EnumType.STRING) private Proficiency proficiency;
 @Column(name = "added_at") final private Instant addedAt = Instant.now();
}
