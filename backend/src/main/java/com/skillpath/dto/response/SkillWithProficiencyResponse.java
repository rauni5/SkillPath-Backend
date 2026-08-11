package com.skillpath.dto.response;
import com.skillpath.model.enums.Proficiency;
import com.skillpath.model.enums.SkillCategory;
import lombok.*;

@Getter @Builder
public class SkillWithProficiencyResponse {
    private Long id;
    private String name;
    private SkillCategory category;
    private Proficiency proficiency;
}
