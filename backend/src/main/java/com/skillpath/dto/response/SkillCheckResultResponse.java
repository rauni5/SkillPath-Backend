package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class SkillCheckResultResponse {
    private int score;
    private int maxScore;
    private String proficiency; // null if the score didn't reach Beginner
    private boolean passed;
    private List<Boolean> correctness;
}