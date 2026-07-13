package com.skillpath.model.RoleRequiredSkill;
import com.skillpath.model.RoleRequiredSkillId;

import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "role_required_skills")
@IdClass(RoleRequiredSkillId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleRequiredSkill {
 @Id @Column(name = "role_id") private Long roleId;
 @Id @Column(name = "skill_id") private Long skillId;
 @Column(nullable = false) private int importance;
}