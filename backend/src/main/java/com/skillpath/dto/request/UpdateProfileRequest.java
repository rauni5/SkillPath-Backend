package com.skillpath.dto.request;
import com.skillpath.model.enums.Proficiency;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 120) 
    private String name;
    @Pattern(regexp = "^[+0-9 ()-]{0,30}$", message = "phoneNumber may only contain digits, spaces, and + ( ) -")
    private String phoneNumber;
    @Size(max = 255)
    @Pattern(regexp = "^(https?://.*)?$", message = "githubUrl must be a valid URL")
    private String githubUrl;
    @Size(max = 255)
    @Pattern(regexp = "^(https?://.*)?$", message = "linkedinUrl must be a valid URL")
    private String linkedinUrl;
    @Size(max = 255)
    private String location;
    @Size(max = 2000)
    private String softSkills;
    @Size(max = 1000) 
    private String bio;
    private Proficiency experienceLevel;
    private Boolean availability;
    private String avatarUrl;
}