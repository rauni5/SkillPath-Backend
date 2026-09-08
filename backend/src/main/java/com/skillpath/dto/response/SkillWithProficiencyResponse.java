package com.skillpath.dto.response;
import com.skillpath.model.enums.Proficiency;
import lombok.*;

@Getter @Builder
public class SkillWithProficiencyResponse {
    private Long id;
    private String name;
    private String category;
    private Proficiency proficiency;
}
