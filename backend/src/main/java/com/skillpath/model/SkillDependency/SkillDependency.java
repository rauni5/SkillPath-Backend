package com.skillpath.model.SkillDependency;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "skill_dependencies")
@IdClass(SkillDependencyId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillDependency {
 @Id @Column(name = "skill_id") private Long skillId;
 @Id @Column(name = "prerequisite_id") private Long prerequisiteId;
}