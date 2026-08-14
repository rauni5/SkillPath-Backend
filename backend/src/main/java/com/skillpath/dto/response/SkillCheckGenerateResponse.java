package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class SkillCheckGenerateResponse {
    private Long attemptId;
    private List<SkillCheckQuestionResponse> questions;
}