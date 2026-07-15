package com.skillpath.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddPortfolioItemRequest {
    private Long projectId;

    @Pattern(
        regexp  = "^(https?://)?([\\w.-]+)(:[0-9]+)?(/.*)?$",
        message = "githubUrl must be a valid URL"
    )
    @Size(max = 500, message = "githubUrl must be 500 characters or fewer")
    private String githubUrl;
    @Size(max = 2000, message = "description must be 2000 characters or fewer")
    private String description;
    @Size(max = 100, message = "userRole must be 100 characters or fewer")
    private String userRole;
}