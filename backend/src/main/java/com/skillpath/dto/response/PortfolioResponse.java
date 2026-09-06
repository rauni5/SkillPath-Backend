package com.skillpath.dto.response;
import com.skillpath.model.enums.Proficiency;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter @Builder
public class PortfolioResponse {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String githubUrl;
    private String linkedinUrl;
    private String location;
    private List<String> softSkills;
    private String bio;
    private String avatarUrl;
    private Proficiency experienceLevel;
    private boolean availability;
    private Instant memberSince;

    /** Null / 0 when the user hasn't set a career goal yet. */
    private String careerGoalRoleName;
    private int careerProgressPercent;

    private List<SkillWithProficiencyResponse> skills;
    /** Projects the user owns or is an accepted member of, deduplicated. */
    private List<ProjectResponse> projects;
    private List<PortfolioItemResponse> portfolioItems;
    private List<CertificationResponse> certifications;
    private List<EducationResponse> education;
}