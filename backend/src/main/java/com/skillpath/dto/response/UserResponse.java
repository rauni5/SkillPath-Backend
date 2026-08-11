package com.skillpath.dto.response;
import com.skillpath.model.User.User;
import com.skillpath.model.enums.Proficiency;
import lombok.*;
import java.time.Instant;
@Getter @Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String githubUrl;
    private String linkedinUrl;
    private String location;
    private String softSkills;
    private String bio;
    private Proficiency experienceLevel;
    private boolean availability;
    private String avatarUrl;
    private Instant createdAt;
    private boolean admin;
    public static UserResponse from(User u) {
        return UserResponse.builder()
        .id(u.getId())
        .name(u.getName())
        .email(u.getEmail())
        .phoneNumber(u.getPhoneNumber())
        .githubUrl(u.getGithubUrl())
        .linkedinUrl(u.getLinkedinUrl())
        .location(u.getLocation())
        .softSkills(u.getSoftSkills())
        .bio(u.getBio())
        .experienceLevel(u.getExperienceLevel())
        .availability(u.isAvailability())
        .avatarUrl(u.getAvatarUrl())
        .createdAt(u.getCreatedAt())
        .admin(u.isAdmin())
        .build();
    }
}