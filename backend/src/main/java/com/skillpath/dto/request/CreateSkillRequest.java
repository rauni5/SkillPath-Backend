package com.skillpath.dto.request;

import com.skillpath.model.enums.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private SkillCategory category;

    @Size(max = 500)
    private String description;
}