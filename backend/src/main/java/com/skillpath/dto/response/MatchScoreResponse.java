package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class MatchScoreResponse {
    private Long userId;
    private String name;
    private String avatarUrl;
    private double matchScore;
}