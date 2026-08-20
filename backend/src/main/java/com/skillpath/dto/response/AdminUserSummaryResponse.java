package com.skillpath.dto.response;
import lombok.*;

/** A user row for the admin Users screen, enriched with per-user stats
 *  so admins can see engagement at a glance without opening each profile. */
@Getter @Builder
public class AdminUserSummaryResponse {
    private UserResponse user;
    private long skillsCount;
    private long ownedProjectsCount;
    private long achievementsCount;
    private boolean careerGoalSet;
}