package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class ProjectInviteResponse {
    private Long projectId;
    private String projectName;
    private String difficulty;
    private Integer teamSize;
}