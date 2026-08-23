package com.skillpath.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillpath.model.AssistantSession.AssistantSession;

import java.util.List;
import java.util.Optional;
public interface AssistantSessionRepository extends JpaRepository<AssistantSession, Long> {
    List<AssistantSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<AssistantSession> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}