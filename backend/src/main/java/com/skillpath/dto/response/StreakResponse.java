package com.skillpath.dto.response;
import lombok.*;
import java.time.LocalDate;
@Getter @Builder @AllArgsConstructor
public class StreakResponse {
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;
}