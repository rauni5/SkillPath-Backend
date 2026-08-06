package com.skillpath.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterDeviceTokenRequest {
    @NotBlank
    private String token;
    private String platform;
}
