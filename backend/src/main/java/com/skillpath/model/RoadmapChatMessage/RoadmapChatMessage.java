package com.skillpath.model.RoadmapChatMessage;
import com.skillpath.model.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity @Table(name = "roadmap_chat_messages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoadmapChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "session_id", nullable = false) private Long sessionId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MessageRole role;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}