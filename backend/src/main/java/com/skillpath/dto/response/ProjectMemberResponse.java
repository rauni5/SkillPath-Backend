package com.skillpath.dto.response;
import com.skillpath.model.enums.MemberStatus;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class ProjectMemberResponse {
    private Long userId;
    private String name;
    private String avatarUrl;
    private MemberStatus status;
    private String role;
}