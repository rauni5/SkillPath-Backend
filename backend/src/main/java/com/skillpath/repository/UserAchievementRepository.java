package com.skillpath.repository;
import com.skillpath.model.UserAchievement.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    boolean existsByAchievementId(Long achievementId);
    long countByAchievementId(Long achievementId);
}