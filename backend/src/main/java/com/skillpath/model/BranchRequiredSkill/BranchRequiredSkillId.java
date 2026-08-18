package com.skillpath.model.BranchRequiredSkill;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class BranchRequiredSkillId implements Serializable {
 private Long branchId;
 private Long skillId;
}