package com.skillpath.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateBranchRequest {
    @NotBlank @Size(max = 100)
    private String name;
    private String description;
}