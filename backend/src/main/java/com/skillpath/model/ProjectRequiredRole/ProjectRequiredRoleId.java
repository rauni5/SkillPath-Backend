package com.skillpath.model.ProjectRequiredRole;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProjectRequiredRoleId implements Serializable {
    private Long projectId;
    private Long roleId;
}