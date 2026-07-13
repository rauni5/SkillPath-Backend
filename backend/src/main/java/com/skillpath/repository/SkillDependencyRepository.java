package com.skillpath.repository;
import com.skillpath.model.SkillDependency.SkillDependency;
import com.skillpath.model.SkillDependency.SkillDependencyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface SkillDependencyRepository
 extends JpaRepository<SkillDependency, SkillDependencyId> {
 List<SkillDependency> findBySkillId(Long skillId);
 @Query("SELECT sd FROM SkillDependency sd")
 List<SkillDependency> findAllDependencies();
}
