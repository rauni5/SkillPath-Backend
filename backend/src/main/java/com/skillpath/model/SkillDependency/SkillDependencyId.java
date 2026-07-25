package com.skillpath.model.SkillDependency;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class SkillDependencyId implements Serializable {
 private Long skillId;
 private Long prerequisiteId;
}
