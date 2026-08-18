package com.skillpath.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillpath.model.AssistantMessage.AssistantMessage;

import java.util.List;
import java.util.Optional;
public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, Long> {
    List<AssistantMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    Optional<AssistantMessage> findTopBySessionIdOrderByCreatedAtDesc(Long sessionId);
}