package com.skillpath.model.ProjectRequiredSkill;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProjectRequiredSkillId implements Serializable {
 private Long projectId;
 private Long skillId;
}