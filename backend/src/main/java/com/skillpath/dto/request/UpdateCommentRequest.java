package com.skillpath.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateCommentRequest {
    @NotBlank @Size(max = 2000)
    private String body;
}