package com.skillpath.dto.response;
import lombok.*;

/** A career role row for the admin Roles screen, enriched with stats so
 *  admins can see which roles are actually configured and chosen without
 *  opening each one. */
@Getter @Builder
public class AdminRoleSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private long requirementsCount;
    /** How many users have set this role as their career goal. */
    private long popularity;
}