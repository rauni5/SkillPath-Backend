package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class BranchRecommendationResponse {
    private Long branchId;
    private String name;
    private String description;
    private double matchScore;
    private List<SkillRef> knownSkills;
    private List<SkillRef> missingSkills;

    @Getter @Builder @AllArgsConstructor
    public static class SkillRef {
        private Long id;
        private String name;
        private int importance;
    }
}