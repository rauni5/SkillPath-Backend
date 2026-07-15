package com.skillpath.dto.response;

import com.skillpath.model.PortfolioItem.PortfolioItem;
import lombok.*;
import java.time.Instant;

@Getter
@Builder
public class PortfolioItemResponse {

    private Long   id;
    private Long   userId;
    private Long   projectId;
    private String projectName;
    private String githubUrl;
    private String description;
    private String userRole;
    private Instant createdAt;

    public static PortfolioItemResponse from(PortfolioItem p) {
        return PortfolioItemResponse.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .projectId(p.getProjectId())
                        .githubUrl(p.getGithubUrl())
                        .description(p.getDescription())
                        .userRole(p.getUserRole())
                        .createdAt(p.getCreatedAt())
                        .build();
    }

    public static PortfolioItemResponse from(PortfolioItem p, String projectName) {
        return PortfolioItemResponse.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .projectId(p.getProjectId())
                        .projectName(projectName)
                        .githubUrl(p.getGithubUrl())
                        .description(p.getDescription())
                        .userRole(p.getUserRole())
                        .createdAt(p.getCreatedAt())
                        .build();
    }
}