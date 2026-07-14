package com.skillpath.service;
import com.skillpath.algorithm.heap.RecommendationHeap;
import com.skillpath.algorithm.matching.TeamMatching;
import com.skillpath.dto.response.MatchScoreResponse;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
@Service @RequiredArgsConstructor
public class RecommendationService {
    private final UserRepository userRepo;
    private final UserSkillRepository userSkillRepo;
    private final UserCareerGoalRepository goalRepo;
    private final ProjectRequiredSkillRepository projSkillRepo;
    private final TeamMatching matcher;
    public List<MatchScoreResponse> recommendTeammates(Long projectId, int topN) {
        Set<Long> required = projSkillRepo.findSkillIdsByProjectId(projectId);
        RecommendationHeap<com.skillpath.model.User.User> heap = new RecommendationHeap<>();
        userRepo.findAll().forEach(user -> {
            Set<Long> skills = userSkillRepo.findSkillIdsByUserId(user.getId());
            Long goalRoleId = goalRepo.findRoleIdByUserId(user.getId()).orElse(null);
            double s = matcher.score(skills, goalRoleId, user.isAvailability(), required, null);
            heap.push(user, s);
        });
        return heap.getTopN(topN).stream()
                                .map(si -> MatchScoreResponse.builder()
                                .userId(si.item().getId()).name(si.item().getName())
                                .avatarUrl(si.item().getAvatarUrl()).matchScore(si.score())
                                .build())
                                .toList();
    }
}
