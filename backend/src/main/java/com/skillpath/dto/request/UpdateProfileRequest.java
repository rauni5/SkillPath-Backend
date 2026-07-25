package com.skillpath.dto.request;
import com.skillpath.model.enums.Proficiency;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 120) 
    private String name;
    @Size(max = 1000) 
    private String bio;
    private Proficiency experienceLevel;
    private Boolean availability;
    private String avatarUrl;
}
