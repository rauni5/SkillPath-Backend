package com.skillpath.repository;
import com.skillpath.model.SkillCheckQuestionSet.SkillCheckQuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SkillCheckQuestionSetRepository extends JpaRepository<SkillCheckQuestionSet, Long> {
    List<SkillCheckQuestionSet> findBySkillId(Long skillId);
}
