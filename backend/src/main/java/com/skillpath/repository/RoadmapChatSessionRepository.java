package com.skillpath.repository;
import com.skillpath.model.RoadmapChatSession.RoadmapChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RoadmapChatSessionRepository extends JpaRepository<RoadmapChatSession, Long> {
    List<RoadmapChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<RoadmapChatSession> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}