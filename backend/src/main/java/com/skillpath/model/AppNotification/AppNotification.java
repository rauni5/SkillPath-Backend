package com.skillpath.model.AppNotification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/**
 * A persisted record of a notification sent via {@code NotificationService
 * .notifyUser}. Push delivery (Firebase) is fire-and-forget and best-effort
 * — this table is what actually backs the in-app notification center, so
 * it exists (and can be listed) independent of whether the push itself
 * ever reached a device.
 */
@Entity @Table(name = "app_notifications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AppNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Matches com.skillpath.service.NotificationType#getValue(), e.g. "PROJECT_INVITE_RECEIVED". */
    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    /** Present for every current notification type; kept nullable for any future type that isn't project-scoped. */
    @Column(name = "project_id")
    private Long projectId;

    /** Only present for discussion-comment notifications. */
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}