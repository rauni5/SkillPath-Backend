package com.skillpath.repository;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.model.UserSkill.UserSkillId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface UserSkillRepository
 extends JpaRepository<UserSkill, UserSkillId> {
 List<UserSkill> findByUserId(Long userId);
 @Query("SELECT us.skillId FROM UserSkill us WHERE us.userId = :userId")
 Set<Long> findSkillIdsByUserId(Long userId);
 void deleteByUserIdAndSkillId(Long userId, Long skillId);

 /** Distinct users who have tracked at least one skill — used for the
  *  "avg skills per user" analytic (denominator excludes users with none,
  *  since including them would make the average misleadingly low). */
 @Query("SELECT COUNT(DISTINCT us.userId) FROM UserSkill us")
 long countDistinctUsers();

 /** Top skills by number of users who have them, most popular first. */
 @Query("""
     SELECT us.skillId AS skillId, COUNT(us) AS cnt
     FROM UserSkill us
     GROUP BY us.skillId
     ORDER BY cnt DESC
     """)
 List<SkillCount> findTopSkills(Pageable pageable);

 /** Batch tracked-skill counts, keyed by user id. */
 @Query("SELECT us.userId AS userId, COUNT(us) AS cnt FROM UserSkill us WHERE us.userId IN :userIds GROUP BY us.userId")
 List<CountByUser> countByUserIds(@org.springframework.data.repository.query.Param("userIds") List<Long> userIds);

 interface CountByUser {
     Long getUserId();
     long getCnt();
 }

 interface SkillCount {
     Long getSkillId();
     long getCnt();
 }
}