package com.skillpath.dto.response;
import lombok.*;
import java.time.LocalDate;
@Getter @Builder
public class DailyCountResponse {
    private LocalDate date;
    private long count;
}
