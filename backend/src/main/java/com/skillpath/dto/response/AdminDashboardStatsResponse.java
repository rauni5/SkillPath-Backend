package com.skillpath.dto.response;
import lombok.*;
import java.util.List;

@Getter @Builder
public class AdminDashboardStatsResponse {
    // Users
    private long totalUsers;
    private long newUsersLast7Days;
    private long newUsersLast30Days;

    // Projects
    private long totalProjects;
    private long openProjects;
    private long completedProjects;

    // Catalogue
    private long totalSkills;
    private long totalCareerRoles;

    // Achievements
    private long totalAchievements;
    private long achievementsUnlockedCount;

    // Engagement
    private double avgSkillsPerUser;
    private List<SkillPopularityResponse> topSkills;
    private List<DailyCountResponse> userSignupTrend;

    // Recent activity
    private List<RecentUserResponse> recentSignups;

    // Deprecated: GA4 wasn't reporting useful data, so this is no longer
    // shown in the UI. Left here (always null now) rather than a breaking
    // API change; FirebaseAnalyticsService still exists if you want it back.
    private FirebaseAnalyticsSummaryResponse firebaseAnalytics;
}