package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
import java.util.Map;

/** Aggregate stats for the admin "Users" screen — computed live from the
 *  database, so it always reflects the current state (no caching). */
@Getter @Builder
public class AdminUserAnalyticsResponse {
    private long totalUsers;
    private long adminCount;
    private long availableCount;
    private long unavailableCount;
    private long usersWithCareerGoalSet;
    private double avgSkillsPerUser;
    private long newUsersLast7Days;
    private long newUsersLast30Days;
    /** Keyed by Proficiency enum name: BEGINNER / INTERMEDIATE / ADVANCED. */
    private Map<String, Long> byExperienceLevel;
    /** One entry per day for the last 30 days, oldest first. */
    private List<DailyCountResponse> signupTrend;
}
