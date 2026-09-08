package com.skillpath.model.Skill;
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
 @Column(nullable = false, length = 50)
 private String category;
 @Column(columnDefinition = "TEXT") 
 private String description;
 @CreationTimestamp @Column(name = "created_at", updatable = false) 
 private Instant createdAt;

 /** Normalizes a free-typed category into the storage convention used by
  *  the original fixed set (e.g. "data engineering" -> "DATA_ENGINEERING"),
  *  so old and new categories look consistent side by side. */
 public static String normalizeCategory(String raw) {
     if (raw == null) return null;
     String trimmed = raw.trim();
     if (trimmed.isEmpty()) return trimmed;
     return trimmed.toUpperCase().replaceAll("[\\s-]+", "_");
 }
}
