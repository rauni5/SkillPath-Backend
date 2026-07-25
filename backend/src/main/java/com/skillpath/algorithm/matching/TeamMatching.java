package com.skillpath.algorithm.matching;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class TeamMatching {
    // Weights
    private static final double SKILL_W = 0.50;
    private static final double GOAL_W = 0.30;
    private static final double AVAIL_W = 0.20;
    /**
     * Returns a 0–100 match score for a user project pair.
     * @param userSkillIds skills the candidate has
     * @param userGoalRoleId the candidate's career goal (null = no goal set)
     * @param available whether the user is open to joining projects
     * @param requiredSkillIds skills the project needs
     * @param projectRoleIds career roles the project targets (null = any)
     */
    public double score(Set<Long> userSkillIds, Long userGoalRoleId, boolean available, Set<Long> requiredSkillIds, Set<Long> projectRoleIds) {
        double skillScore = jaccard(userSkillIds, requiredSkillIds);
        double goalScore = (projectRoleIds != null && projectRoleIds.contains(userGoalRoleId)) ? 1.0 : 0.0;
        double availScore = available ? 1.0 : 0.0;
        return Math.round((skillScore*SKILL_W + goalScore*GOAL_W + availScore*AVAIL_W) * 100.0);
    }

    private double jaccard(Set<Long> a, Set<Long> b) {
        if (b == null || b.isEmpty()) return 0.0;
        Set<Long> inter = new HashSet<>(a); 
        inter.retainAll(b);
        Set<Long> union = new HashSet<>(a); 
        union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }
}
