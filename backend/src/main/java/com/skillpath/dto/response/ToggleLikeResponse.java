package com.skillpath.dto.response;
import lombok.*;

@Getter @Builder
public class ToggleLikeResponse {
    private boolean liked;
    private int likeCount;
}