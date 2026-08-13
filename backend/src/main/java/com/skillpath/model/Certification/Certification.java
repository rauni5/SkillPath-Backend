package com.skillpath.model.Certification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name = "certifications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Certification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String issuer;

    @Column(name = "credential_url", length = 500)
    private String credentialUrl;

    @Column(name = "earned_on")
    private LocalDate earnedOn;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}