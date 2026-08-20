package com.skillpath.model.User;
import com.skillpath.model.enums.Proficiency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 // Firebase UID — primary link between Firebase Auth and our DB
 @Column(name = "firebase_uid", unique = true, nullable = false)
 private String firebaseUid;
 @Column private String email;
 @Column(length = 120) private String name;
 @Column(name = "phone_number", length = 30) private String phoneNumber;
 @Column(name = "github_url", length = 255) private String githubUrl;
 @Column(name = "linkedin_url", length = 255) private String linkedinUrl;
 @Column(length = 255) private String location;
 @Column(name = "soft_skills", columnDefinition = "TEXT") private String softSkills;
 @Column(columnDefinition = "TEXT") private String bio;
 @Enumerated(EnumType.STRING)
 @Column(name = "experience_level")
 private Proficiency experienceLevel;
 @Column(nullable = false) @Builder.Default
 private boolean availability = true;
 @Column(name = "avatar_url", length = 500) private String avatarUrl;
 @CreationTimestamp
 @Column(name = "created_at", updatable = false)
 private Instant createdAt;
 @Column(name = "is_admin", nullable = false)
 @Builder.Default
 private boolean admin = false;
 @Column(nullable = false)@Builder.Default
 private boolean active = true;
}