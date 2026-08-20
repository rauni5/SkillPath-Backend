package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder
public class GapAnalysisResponse {
    private String careerRoleName;
    private Long branchId;
    private String branchName;
    private int progressPercent;
    private int knownSkillCount;
    private int requiredSkillCount;
    private List<KnownSkill> knownSkills;
    private List<MissingSkill> missingSkills;
    @Getter @Builder @AllArgsConstructor
    public static class MissingSkill {
        private Long id;
        private String name;
        private int importance;
    }
    @Getter @Builder @AllArgsConstructor
    public static class KnownSkill {
        private Long id;
        private String name;
        private int importance;
    }
}