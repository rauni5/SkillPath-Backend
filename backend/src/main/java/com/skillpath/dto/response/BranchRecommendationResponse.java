package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class BranchRecommendationResponse {
    private Long branchId;
    private String name;
    private String description;
    private double matchScore;
}