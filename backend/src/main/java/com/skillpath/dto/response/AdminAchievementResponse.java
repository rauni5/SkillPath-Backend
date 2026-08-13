package com.skillpath.dto.response;
import com.skillpath.model.Achievement.Achievement;
import com.skillpath.model.enums.AchievementCriteriaType;
import lombok.*;

@Getter @Builder
public class AdminAchievementResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private String icon;
    private String category;
    private AchievementCriteriaType criteriaType;
    private int criteriaValue;
    private boolean enabled;
    /** How many users currently have this unlocked — informational, helps
     *  an admin gauge whether an achievement is too easy/hard, and shows
     *  up as a hint before they try to delete one. */
    private long unlockedByCount;

    public static AdminAchievementResponse from(Achievement a, long unlockedByCount) {
        return AdminAchievementResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .title(a.getTitle())
                .description(a.getDescription())
                .icon(a.getIcon())
                .category(a.getCategory())
                .criteriaType(a.getCriteriaType())
                .criteriaValue(a.getCriteriaValue())
                .enabled(a.isEnabled())
                .unlockedByCount(unlockedByCount)
                .build();
    }
}