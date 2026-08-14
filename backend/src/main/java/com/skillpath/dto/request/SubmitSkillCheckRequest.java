package com.skillpath.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitSkillCheckRequest {
    @NotNull
    private Long attemptId;
    /** answers[i] = the option index the user picked for question i (0-based). */
    @NotEmpty
    private List<Integer> answers;
}