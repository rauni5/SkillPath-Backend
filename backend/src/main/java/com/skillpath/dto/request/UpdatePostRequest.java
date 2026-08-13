package com.skillpath.dto.request;
import com.skillpath.model.enums.PostTag;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdatePostRequest {
    private PostTag tag;

    @NotBlank @Size(max = 200)
    private String title;

    @NotBlank @Size(max = 5000)
    private String body;
}