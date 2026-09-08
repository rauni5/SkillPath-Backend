package com.skillpath.dto.response;
import com.skillpath.model.Skill.Skill;
import lombok.*;
@Getter @Builder
public class SkillResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    public static SkillResponse from(Skill s) {
        return SkillResponse.builder()
        .id(s.getId())
        .name(s.getName())
        .category(s.getCategory())
        .description(s.getDescription())
        .build();
    }
}