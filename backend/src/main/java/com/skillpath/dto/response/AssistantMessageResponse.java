package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
@Getter @Builder @AllArgsConstructor
public class AssistantMessageResponse {
    private Long id;
    private String role;
    private String content;
    private Instant createdAt;
}