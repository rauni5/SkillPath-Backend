package com.skillpath.dto.response;
import com.skillpath.model.enums.AchievementCriteriaType;
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
    private AchievementCriteriaType criteriaType;
    private int criteriaValue;
}