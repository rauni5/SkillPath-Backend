package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder
public class GapAnalysisResponse {
    private String careerRoleName;
    private int progressPercent;
    private int knownSkillCount;
    private int requiredSkillCount;
    private List<MissingSkill> missingSkills;
    @Getter @Builder @AllArgsConstructor
    public static class MissingSkill {
        private Long id;
        private String name;
        private int importance;
    }
}
