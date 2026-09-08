package com.skillpath.repository;
import com.skillpath.model.Skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface SkillRepository extends JpaRepository<Skill, Long> {
 List<Skill> findByNameContainingIgnoreCase(String prefix);
 boolean existsByNameIgnoreCase(String name);

 /** Distinct categories currently in use, alphabetically — lets the admin
  *  UI offer existing categories as suggestions without hardcoding a list. */
 @Query("select distinct s.category from Skill s order by s.category asc")
 List<String> findDistinctCategories();
}