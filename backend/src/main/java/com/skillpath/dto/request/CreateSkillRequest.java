package com.skillpath.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    
    @NotBlank
    @Size(max = 50)
    private String category;

    @Size(max = 500)
    private String description;
}