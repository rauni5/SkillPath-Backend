package com.skillpath.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddDependencyRequest {

    @NotNull(message = "prerequisiteId is required")
    private Long prerequisiteId;
}