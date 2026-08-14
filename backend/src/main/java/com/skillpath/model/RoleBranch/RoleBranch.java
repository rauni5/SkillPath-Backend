package com.skillpath.model.RoleBranch;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "role_branches")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleBranch {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "role_id", nullable = false) private Long roleId;
 @Column(nullable = false, length = 100) private String name;
 @Column(columnDefinition = "TEXT") private String description;
}