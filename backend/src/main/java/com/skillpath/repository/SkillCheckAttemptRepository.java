package com.skillpath.repository;
import com.skillpath.model.SkillCheckAttempt.SkillCheckAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SkillCheckAttemptRepository extends JpaRepository<SkillCheckAttempt, Long> {
}