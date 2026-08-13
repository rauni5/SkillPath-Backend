package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class RoleBranchResponse {
    private Long id;
    private Long roleId;
    private String name;
    private String description;
}