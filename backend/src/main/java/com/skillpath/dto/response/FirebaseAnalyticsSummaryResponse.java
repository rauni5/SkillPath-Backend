package com.skillpath.dto.response;
import lombok.*;
import java.util.List;

/** Summary pulled from the GA4 (Firebase Analytics) Data API. `configured`
 *  is false — with every numeric field left at 0 — when the backend hasn't
 *  been given a GA4 property ID, so the admin UI can show a setup hint
 *  instead of a broken chart. `available` is false when a property ID is
 *  set but the live call to Google failed (e.g. bad credentials, property
 *  not yet collecting data); `error` then carries a short reason. */
@Getter @Builder
public class FirebaseAnalyticsSummaryResponse {
    private boolean configured;
    private boolean available;
    private String error;
    private long activeUsers7Day;
    private long activeUsers28Day;
    private long screenPageViewsLast30Days;
    private List<TopEvent> topEvents;

    @Getter @Builder
    public static class TopEvent {
        private String name;
        private long count;
    }

    public static FirebaseAnalyticsSummaryResponse notConfigured() {
        return FirebaseAnalyticsSummaryResponse.builder()
                .configured(false).available(false).build();
    }

    public static FirebaseAnalyticsSummaryResponse failed(String reason) {
        return FirebaseAnalyticsSummaryResponse.builder()
                .configured(true).available(false).error(reason).build();
    }
}
