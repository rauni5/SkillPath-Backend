package com.skillpath.repository;
import com.skillpath.model.ProjectRequiredSkill.ProjectRequiredSkill;
import com.skillpath.model.ProjectRequiredSkill.ProjectRequiredSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface ProjectRequiredSkillRepository
 extends JpaRepository<ProjectRequiredSkill, ProjectRequiredSkillId> {
 List<ProjectRequiredSkill> findByProjectId(Long projectId);
 @Query("SELECT p.skillId FROM ProjectRequiredSkill p WHERE p.projectId = :id")
 Set<Long> findSkillIdsByProjectId(Long id);
}