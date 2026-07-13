package com.skillpath.dto.response;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import com.skillpath.model.enums.StepStatus;
import lombok.*;
import java.time.Instant;
@Getter @Builder
public class RoadmapStepResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private int stepOrder;
    private StepStatus status;
    private Instant completedAt;
    public static RoadmapStepResponse from(RoadmapStep s,String skillName, String category) {
        return RoadmapStepResponse.builder()
                .id(s.getId())
                .skillId(s.getSkillId())
                .skillName(skillName)
                .skillCategory(category)
                .stepOrder(s.getStepOrder())
                .status(s.getStatus())
                .completedAt(s.getCompletedAt())
                .build();
    }
}