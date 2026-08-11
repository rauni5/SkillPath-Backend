package com.skillpath.dto.request;
import com.skillpath.model.enums.AchievementCriteriaType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateAchievementRequest {
    /** Short, unique, machine-readable identifier — e.g. "NIGHT_OWL". Immutable after creation. */
    @NotBlank @Size(max = 50) @Pattern(
            regexp = "^[A-Z0-9_]+$",
            message = "Code must be uppercase letters, numbers, and underscores only")
    private String code;

    @NotBlank @Size(max = 100)
    private String title;

    @NotBlank @Size(max = 255)
    private String description;

    @NotBlank @Size(max = 50)
    private String icon;

    @NotBlank @Size(max = 30)
    private String category;

    @NotNull
    private AchievementCriteriaType criteriaType;

    @Min(1)
    private int criteriaValue;
}