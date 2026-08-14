package com.skillpath.model.ChatMessage;
import com.skillpath.model.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity @Table(name = "chat_messages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "user_id", nullable = false) private Long userId;
 @Column(name = "skill_id", nullable = false) private Long skillId;
 @Enumerated(EnumType.STRING) @Column(nullable = false) private MessageRole role;
 @Column(nullable = false, columnDefinition = "TEXT") private String content;
 @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}