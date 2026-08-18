package com.skillpath.model.BranchRequiredSkill;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "branch_required_skills")
@IdClass(BranchRequiredSkillId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchRequiredSkill {
 @Id @Column(name = "branch_id") private Long branchId;
 @Id @Column(name = "skill_id") private Long skillId;
 @Column(nullable = false) private int importance;
}