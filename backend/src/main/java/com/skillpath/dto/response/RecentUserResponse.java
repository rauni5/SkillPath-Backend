package com.skillpath.dto.response;
import lombok.*;
import java.time.Instant;

@Getter @Builder
public class RecentUserResponse {
    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
    private Instant createdAt;
}