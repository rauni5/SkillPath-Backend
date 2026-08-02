package com.skillpath.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateProjectRequest {
    @NotBlank @Size(max = 200) private String name;
    private String description;
    private String difficulty;
    @Size(max = 500) private String link;
    @Min(1) @Max(20) private Integer teamSize;
    private Set<Long> requiredSkillIds;
    private Set<Long> requiredRoleIds;
}