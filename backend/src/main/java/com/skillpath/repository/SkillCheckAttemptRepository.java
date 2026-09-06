package com.skillpath.repository;
import com.skillpath.model.SkillCheckAttempt.SkillCheckAttempt;
import com.skillpath.model.enums.SkillCheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SkillCheckAttemptRepository extends JpaRepository<SkillCheckAttempt, Long> {
    long countByUserIdAndStatusAndProficiencyIsNotNull(Long userId, SkillCheckStatus status);
    List<SkillCheckAttempt> findByUserIdAndStatusAndProficiencyIsNotNull(Long userId, SkillCheckStatus status);

    /** Every question-set this user has already been given for this skill
     *  (across all attempts, submitted or not) — used to pick a set they
     *  haven't seen yet, or detect they've exhausted the pool. */
    List<SkillCheckAttempt> findByUserIdAndSkillIdAndQuestionSetIdIsNotNull(Long userId, Long skillId);
}
