package com.skillpath.repository;
import com.skillpath.model.Skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SkillRepository extends JpaRepository<Skill, Long> {
 List<Skill> findByNameContainingIgnoreCase(String prefix);
 boolean existsByNameIgnoreCase(String name);
}