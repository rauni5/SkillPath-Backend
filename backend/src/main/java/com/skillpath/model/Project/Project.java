package com.skillpath.model.Project;
import com.skillpath.model.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "projects")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Project {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(nullable = false, length = 200) private String name;
 @Column(columnDefinition = "TEXT") private String description;
 @Column(length = 20) private String difficulty;
 @Column(name = "team_size") private Integer teamSize;
 @Enumerated(EnumType.STRING) @Builder.Default
 private ProjectStatus status = ProjectStatus.OPEN;
 @Column(name = "owner_id") private Long ownerId;
 @Column(length = 500) private String link;
 @CreationTimestamp @Column(name = "created_at", updatable = false)
 private Instant createdAt;
}