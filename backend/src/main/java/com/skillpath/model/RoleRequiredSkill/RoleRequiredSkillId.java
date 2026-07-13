package com.skillpath.model.RoleRequiredSkill;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RoleRequiredSkillId implements Serializable {
 private Long roleId;
 private Long skillId;
}
