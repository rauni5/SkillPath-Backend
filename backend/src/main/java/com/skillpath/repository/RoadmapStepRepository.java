package com.skillpath.repository;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long>
{
 List<RoadmapStep> findByUserIdOrderByStepOrder(Long userId);
 List<RoadmapStep> findByUserIdAndSkillId(Long userId, Long skillId);
 void deleteByUserId(Long userId);
}
