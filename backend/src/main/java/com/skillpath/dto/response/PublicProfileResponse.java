package com.skillpath.dto.response;
import com.skillpath.model.enums.Proficiency;
import lombok.*;
import java.time.Instant;
import java.util.List;

/**
 * Deliberately a separate, smaller shape from {@link PortfolioResponse} —
 * that DTO is calibrated for another *authenticated* SkillPath user
 * viewing your profile in-app, and includes things (email, full project
 * membership) that shouldn't go to a completely anonymous visitor who
 * doesn't even need an account to see this. Never includes: email, phone
 * number, or location. `githubUrl`/`linkedinUrl` are included since
 * they're themselves meant to be public-facing links.
 */
@Getter @Builder
public class PublicProfileResponse {
    private Long userId;
    private String name;
    private String avatarUrl;
    private String bio;
    private List<String> softSkills;
    private Proficiency experienceLevel;
    private boolean availability;
    private String githubUrl;
    private String linkedinUrl;
    private Instant memberSince;

    /** Null when no career goal is set yet. */
    private String careerGoalRoleName;
    private int careerProgressPercent;

    private List<SkillWithProficiencyResponse> skills;
    private List<AchievementResponse> achievements;
    private List<ProjectResponse> projects;
    private List<PortfolioItemResponse> portfolioItems;
    private List<CertificationResponse> certifications;
    private List<EducationResponse> education;
}
