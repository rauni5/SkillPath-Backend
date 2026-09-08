package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter 
@Builder 
@AllArgsConstructor
public class ProjectJoinRequestResponse {
    private Long projectId;
    private String projectName;
    private Long requesterId;
    private String requesterName;
    private String requesterAvatarUrl;
    private List<SkillResponse> requesterSkills;
}