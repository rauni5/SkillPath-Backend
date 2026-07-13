package com.skillpath.model.ProjectRequiredSkill;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "project_required_skills")
@IdClass(ProjectRequiredSkillId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectRequiredSkill {
 @Id @Column(name = "project_id") private Long projectId;
 @Id @Column(name = "skill_id") private Long skillId;
}
