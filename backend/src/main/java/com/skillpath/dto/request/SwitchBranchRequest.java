package com.skillpath.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SwitchBranchRequest {
    @NotNull
    private Long branchId;
}