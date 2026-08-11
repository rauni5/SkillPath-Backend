package com.skillpath.repository;
import com.skillpath.model.RoadmapChatMessage.RoadmapChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RoadmapChatMessageRepository extends JpaRepository<RoadmapChatMessage, Long> {
    List<RoadmapChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    Optional<RoadmapChatMessage> findTopBySessionIdOrderByCreatedAtDesc(Long sessionId);
}