package com.skillpath.dto.response;
import com.skillpath.model.User.User;
import com.skillpath.model.enums.Proficiency;
import lombok.*;

@Getter @Builder
public class UserSearchResultResponse {
    private Long id;
    private String name;
    private String avatarUrl;
    private String bio;
    private Proficiency experienceLevel;
    private boolean availability;

    public static UserSearchResultResponse from(User u) {
        return UserSearchResultResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .avatarUrl(u.getAvatarUrl())
                .bio(u.getBio())
                .experienceLevel(u.getExperienceLevel())
                .availability(u.isAvailability())
                .build();
    }
}