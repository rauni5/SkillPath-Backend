package com.skillpath.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SendChatMessageRequest {
    @NotBlank @Size(max = 4000)
    private String message;
}