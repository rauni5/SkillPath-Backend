package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
@Getter @Builder @AllArgsConstructor
public class RoadmapChatSessionResponse {
    private Long id;
    private String title;
    private Instant createdAt;
    /** True only for the single most-recent session for this user — the only one that accepts new messages. */
    private boolean active;
    private String lastMessagePreview;
}