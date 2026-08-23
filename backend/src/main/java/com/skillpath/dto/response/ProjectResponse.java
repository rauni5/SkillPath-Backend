package com.skillpath.dto.response;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.model.Project.Project;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.ProjectStatus;
import lombok.*;
import java.time.Instant;
import java.util.List;
@Getter @Setter @Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private String link;
    private Integer teamSize;
    private ProjectStatus status;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;
    private List<SkillResponse> requiredSkills;
    private List<CareerRole> requiredRoles;
    private Instant createdAt;
    private MemberStatus viewerMembershipStatus;
    private boolean viewerInvitedByOwner;
    public static ProjectResponse from(Project p) {
        return ProjectResponse.builder()
        .id(p.getId())
        .name(p.getName())
        .description(p.getDescription())
        .difficulty(p.getDifficulty())
        .link(p.getLink())
        .teamSize(p.getTeamSize())
        .status(p.getStatus())
        .ownerId(p.getOwnerId())
        .createdAt(p.getCreatedAt())
        .build();
    }
}