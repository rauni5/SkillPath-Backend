package com.skillpath.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCareerRoleRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}