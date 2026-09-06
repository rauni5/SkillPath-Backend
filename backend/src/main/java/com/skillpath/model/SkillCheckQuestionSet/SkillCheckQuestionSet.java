package com.skillpath.model.SkillCheckQuestionSet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/** One AI-generated quiz for a skill, shared across every user who takes
 *  that skill's check. The pool for a skill grows lazily — a new set is
 *  only generated when a user has already attempted every set that
 *  currently exists for that skill. */
@Entity @Table(name = "skill_check_question_sets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillCheckQuestionSet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "skill_id", nullable = false) private Long skillId;

    /** Same raw shape Gemini returns: {"questions":[{question, options, correctIndex}, ...]} */
    @Column(name = "questions_json", nullable = false, columnDefinition = "TEXT")
    private String questionsJson;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}
