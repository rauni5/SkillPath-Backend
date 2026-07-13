package com.skillpath.model.Skill;
import com.skillpath.model.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "skills")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Skill {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
 private Long id;
 @Column(unique = true, nullable = false, length = 100) 
 private String name;
 @Enumerated(EnumType.STRING) @Column(nullable = false) 
 private SkillCategory category;
 @Column(columnDefinition = "TEXT") 
 private String description;
 @CreationTimestamp @Column(name = "created_at", updatable = false) 
 private Instant createdAt;
}
