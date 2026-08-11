package com.skillpath.dto.response;
import lombok.*;

/** Result of an achievement deletion attempt. If any user had already
 *  unlocked it, it's disabled instead of removed (so their earned badge
 *  stays intact) — `deleted` is false and `achievement` reflects the new
 *  disabled state. Otherwise it's actually removed and `achievement` is null. */
@Getter @Builder
public class AchievementDeletionResult {
    private boolean deleted;
    private String message;
    private AdminAchievementResponse achievement;
}