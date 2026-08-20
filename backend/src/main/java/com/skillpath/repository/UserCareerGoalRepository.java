package com.skillpath.repository;
import com.skillpath.model.UserCareerGoal.UserCareerGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface UserCareerGoalRepository
 extends JpaRepository<UserCareerGoal, Long> {
 Optional<UserCareerGoal> findByUserId(Long userId);
 @Query("SELECT g.roleId FROM UserCareerGoal g WHERE g.userId = :userId")
 Optional<Long> findRoleIdByUserId(Long userId);

 /** Which of the given user ids have a career goal set — used to batch
  *  this check for a whole page of the admin user list. */
 @Query("SELECT g.userId FROM UserCareerGoal g WHERE g.userId IN :userIds")
 List<Long> findUserIdsWithGoalSet(@Param("userIds") List<Long> userIds);

 /** How many users chose each role as their career goal — used for the
  *  admin roles list's "popularity" badge and the top-choices chart. */
 @Query("SELECT g.roleId AS roleId, COUNT(g) AS cnt FROM UserCareerGoal g GROUP BY g.roleId")
 List<CountByRole> countGroupedByRole();

 interface CountByRole {
     Long getRoleId();
     long getCnt();
 }
}