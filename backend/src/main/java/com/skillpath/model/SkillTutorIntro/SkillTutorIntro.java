package com.skillpath.model.SkillTutorIntro;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/** The cached "welcome to this skill" tutor message — one per skill,
 *  shared across every student, since it's generic pedagogy rather than
 *  anything personalized. */
@Entity @Table(name = "skill_tutor_intros")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillTutorIntro {
    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "intro_text", nullable = false, columnDefinition = "TEXT")
    private String introText;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}
