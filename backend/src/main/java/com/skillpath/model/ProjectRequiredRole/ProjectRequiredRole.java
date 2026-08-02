package com.skillpath.model.ProjectRequiredRole;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "project_required_roles")
@IdClass(ProjectRequiredRoleId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectRequiredRole {
    @Id @Column(name = "project_id") private Long projectId;
    @Id @Column(name = "role_id") private Long roleId;
}