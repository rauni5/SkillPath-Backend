package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
@Getter @Builder @AllArgsConstructor
public class AchievementResponse {
    private String code;
    private String title;
    private String description;
    private String icon;
    private String category;
    private boolean unlocked;
    private Instant unlockedAt;
}