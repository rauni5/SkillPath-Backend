package com.skillpath.dto.request;
import com.skillpath.model.enums.AchievementCriteriaType;
import jakarta.validation.constraints.*;
import lombok.*;

/** Code is intentionally not editable — it's the stable identifier other
 *  systems (and the seed data) reference. Everything else can change. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateAchievementRequest {
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

    @NotNull
    private Boolean enabled;
}