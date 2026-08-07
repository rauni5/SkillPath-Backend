package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
@Getter @Builder @AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String role;
    private String content;
    private Instant createdAt;
}