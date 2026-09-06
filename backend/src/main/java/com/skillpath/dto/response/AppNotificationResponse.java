package com.skillpath.dto.response;

import com.skillpath.model.AppNotification.AppNotification;
import lombok.*;
import java.time.Instant;

@Getter @Builder
public class AppNotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private Long projectId;
    private Long postId;
    private boolean read;
    private Instant createdAt;

    public static AppNotificationResponse from(AppNotification n) {
        return AppNotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .projectId(n.getProjectId())
                .postId(n.getPostId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}