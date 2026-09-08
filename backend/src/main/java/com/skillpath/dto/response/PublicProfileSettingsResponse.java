package com.skillpath.dto.response;
import lombok.*;

/** Returned by the authenticated public-profile settings endpoints
 *  (get/enable/disable/regenerate) — never returned by the public,
 *  unauthenticated endpoint itself. */
@Getter @Builder
public class PublicProfileSettingsResponse {
    private boolean enabled;
    /** Null when never enabled yet. Present (even while disabled) once
     *  generated once, so re-enabling doesn't silently invalidate a link
     *  someone might have shared before turning it off. */
    private String token;
}
