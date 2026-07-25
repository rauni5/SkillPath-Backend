package com.skillpath.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddRequirementRequest {

    @NotNull
    private Long skillId;

    @Min(1)
    @Max(10)
    private int importance;
}