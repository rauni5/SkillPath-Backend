package com.skillpath.repository;
import com.skillpath.model.UserAchievement.UserAchievement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    boolean existsByAchievementId(Long achievementId);
    long countByAchievementId(Long achievementId);
    long countByUserId(Long userId);

    /** Batch unlocked-achievement counts, keyed by user id. */
    @Query("SELECT ua.userId AS userId, COUNT(ua) AS cnt FROM UserAchievement ua WHERE ua.userId IN :userIds GROUP BY ua.userId")
    List<CountByUser> countByUserIds(@Param("userIds") List<Long> userIds);

    interface CountByUser {
        Long getUserId();
        long getCnt();
    }
}