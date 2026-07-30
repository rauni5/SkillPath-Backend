package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.RoadmapStepResponse;
import com.skillpath.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;
    @GetMapping("/api/v1/users/{userId}/roadmap")
    public ResponseEntity<ApiResponse<List<RoadmapStepResponse>>> get(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(roadmapService.getRoadmap(userId)));
    }
    @PatchMapping("/api/v1/users/{userId}/roadmap/{stepId}")
    public ResponseEntity<ApiResponse<RoadmapStepResponse>> markDone(@PathVariable Long userId, @PathVariable Long stepId) {
        return ResponseEntity.ok(ApiResponse.ok(roadmapService.markDone(userId, stepId)));
    }
}
