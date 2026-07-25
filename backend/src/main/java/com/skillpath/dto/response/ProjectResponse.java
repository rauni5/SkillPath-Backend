package com.skillpath.dto.response;
import com.skillpath.model.Project.Project;
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
    private Integer teamSize;
    private ProjectStatus status;
    private Long ownerId;
    private List<SkillResponse> requiredSkills;
    private Instant createdAt;
    public static ProjectResponse from(Project p) {
        return ProjectResponse.builder()
        .id(p.getId())
        .name(p.getName())
        .description(p.getDescription())
        .difficulty(p.getDifficulty())
        .teamSize(p.getTeamSize())
        .status(p.getStatus())
        .ownerId(p.getOwnerId())
        .createdAt(p.getCreatedAt())
        .build();
    }
}
