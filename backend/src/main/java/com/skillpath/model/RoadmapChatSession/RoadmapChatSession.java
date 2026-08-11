package com.skillpath.model.RoadmapChatSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/** One conversation thread of the general (not skill-specific) roadmap chat.
 *  Only the most recently created session per user is writable — older ones
 *  stay visible as read-only history. */
@Entity @Table(name = "roadmap_chat_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoadmapChatSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    /** Set from the first user message once sent; null for a brand-new, empty session. */
    @Column(length = 120) private String title;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}