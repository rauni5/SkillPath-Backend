package com.skillpath.repository;
import com.skillpath.model.Achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    boolean existsByCodeIgnoreCase(String code);
}