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
        .bio(u.getBio())
        .experienceLevel(u.getExperienceLevel())
        .availability(u.isAvailability())
        .avatarUrl(u.getAvatarUrl())
        .createdAt(u.getCreatedAt())
        .admin(u.isAdmin())
        .build();
    }
}