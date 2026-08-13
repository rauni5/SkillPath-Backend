package com.skillpath.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddCertificationRequest {
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 200)
    private String issuer;
    @Pattern(
        regexp  = "^(https?://)?([\\w.-]+)(:[0-9]+)?(/.*)?$",
        message = "credentialUrl must be a valid URL"
    )
    @Size(max = 500)
    private String credentialUrl;
    private LocalDate earnedOn;
}