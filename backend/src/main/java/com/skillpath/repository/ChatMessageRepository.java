package com.skillpath.repository;
import com.skillpath.model.ChatMessage.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
 List<ChatMessage> findByUserIdAndSkillIdOrderByCreatedAtAsc(Long userId, Long skillId);
 List<ChatMessage> findByUserIdAndSkillIdOrderByCreatedAtDesc(Long userId, Long skillId, Pageable pageable);
}