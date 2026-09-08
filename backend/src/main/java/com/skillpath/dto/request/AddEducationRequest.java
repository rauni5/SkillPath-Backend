package com.skillpath.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddEducationRequest {
    @NotBlank
    @Size(max = 200)
    private String institution;
    @Size(max = 150)
    private String degree;
    @Size(max = 150)
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 2000)
    private String description;
}