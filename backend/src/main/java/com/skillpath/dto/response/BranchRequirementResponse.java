package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class BranchRequirementResponse {
    private Long skillId;
    private String name;
    private String category;
    private int importance;
}