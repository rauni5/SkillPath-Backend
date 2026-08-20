package com.skillpath.repository;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.model.UserSkill.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface UserSkillRepository
 extends JpaRepository<UserSkill, UserSkillId> {
 List<UserSkill> findByUserId(Long userId);
 @Query("SELECT us.skillId FROM UserSkill us WHERE us.userId = :userId")
 Set<Long> findSkillIdsByUserId(Long userId);
 void deleteByUserIdAndSkillId(Long userId, Long skillId);
}
