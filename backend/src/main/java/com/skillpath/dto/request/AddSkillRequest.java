package com.skillpath.dto.request;
import com.skillpath.model.enums.Proficiency;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddSkillRequest {
    @NotNull 
    private Long skillId;
    @NotNull 
    private Proficiency proficiency;
}