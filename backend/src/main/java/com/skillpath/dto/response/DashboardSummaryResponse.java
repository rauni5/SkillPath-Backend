package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
@Getter @Builder @AllArgsConstructor
public class DashboardSummaryResponse {
    private String content;
    private Instant generatedAt;
}