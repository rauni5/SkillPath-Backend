package com.skillpath.dto.request;
import com.skillpath.model.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateMemberStatusRequest {
    @NotNull 
    private MemberStatus status;
}
