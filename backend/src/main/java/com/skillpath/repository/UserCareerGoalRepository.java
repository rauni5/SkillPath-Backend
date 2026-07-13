package com.skillpath.repository;
import com.skillpath.model.UserCareerGoal.UserCareerGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
public interface UserCareerGoalRepository
 extends JpaRepository<UserCareerGoal, Long> {
 Optional<UserCareerGoal> findByUserId(Long userId);
 @Query("SELECT g.roleId FROM UserCareerGoal g WHERE g.userId = :userId")
 Optional<Long> findRoleIdByUserId(Long userId);
}
