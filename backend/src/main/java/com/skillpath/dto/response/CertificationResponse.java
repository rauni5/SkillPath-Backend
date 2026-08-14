package com.skillpath.dto.response;

import com.skillpath.model.Certification.Certification;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Getter @Builder
public class CertificationResponse {
    private Long id;
    private Long userId;
    private String name;
    private String issuer;
    private String credentialUrl;
    private LocalDate earnedOn;
    private Instant createdAt;

    public static CertificationResponse from(Certification c) {
        return CertificationResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .name(c.getName())
                .issuer(c.getIssuer())
                .credentialUrl(c.getCredentialUrl())
                .earnedOn(c.getEarnedOn())
                .createdAt(c.getCreatedAt())
                .build();
    }
}