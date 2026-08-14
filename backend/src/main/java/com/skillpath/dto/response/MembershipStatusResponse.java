package com.skillpath.dto.response;
import com.skillpath.model.enums.MemberStatus;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class MembershipStatusResponse {
    private Long projectId;
    private String projectName;
    private MemberStatus status;
}