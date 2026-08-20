package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder
public class DashboardResponse {
    private int careerProgressPercent;
    private int roadmapCompletedSteps;
    private int roadmapTotalSteps;
    private String careerRoleName;
    private String branchName;
    private List<ProjectResponse> activeProjects;
    private List<SkillResponse> nextSkillsToLearn;
}