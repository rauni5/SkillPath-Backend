package com.skillpath.dto.response;
import lombok.*;
@Getter @Builder
public class SkillPopularityResponse {
    private Long skillId;
    private String name;
    private long userCount;
}
