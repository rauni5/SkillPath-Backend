package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter @Builder @AllArgsConstructor
public class ProjectInviteResponse {
    private Long projectId;
    private String projectName;
    private String description;
    private String difficulty;
    private Integer teamSize;
    private Integer memberCount;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;
    private List<SkillResponse> requiredSkills;
    private Instant invitedAt;
}