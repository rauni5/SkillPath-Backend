package com.skillpath.dto.response;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

import com.skillpath.model.Education.Education;

@Getter @Builder
public class EducationResponse {
    private Long id;
    private Long userId;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Instant createdAt;

    public static EducationResponse from(Education e) {
        return EducationResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .institution(e.getInstitution())
                .degree(e.getDegree())
                .fieldOfStudy(e.getFieldOfStudy())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }
}