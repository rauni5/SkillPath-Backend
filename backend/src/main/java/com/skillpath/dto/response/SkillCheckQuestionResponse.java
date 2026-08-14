package com.skillpath.dto.response;
import lombok.*;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class SkillCheckQuestionResponse {
    private int index;
    private String question;
    private List<String> options;
}