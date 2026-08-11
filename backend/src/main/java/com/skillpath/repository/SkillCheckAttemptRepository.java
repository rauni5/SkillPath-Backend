package com.skillpath.repository;
import com.skillpath.model.SkillCheckAttempt.SkillCheckAttempt;
import com.skillpath.model.enums.SkillCheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SkillCheckAttemptRepository extends JpaRepository<SkillCheckAttempt, Long> {
    long countByUserIdAndStatusAndProficiencyIsNotNull(Long userId, SkillCheckStatus status);
    List<SkillCheckAttempt> findByUserIdAndStatusAndProficiencyIsNotNull(Long userId, SkillCheckStatus status);
}