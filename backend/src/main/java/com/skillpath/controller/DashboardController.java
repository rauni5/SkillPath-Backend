package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.DashboardResponse;
import com.skillpath.dto.response.DashboardSummaryResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.DashboardAiService;
import com.skillpath.service.DashboardService;
import com.skillpath.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final DashboardAiService dashboardAiService;
    private final UserService userService;

    @GetMapping("/api/v1/users/{userId}/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard(userId)));
    }

    /** Returns the last AI-generated summary, or null data if none has been generated yet. */
    @GetMapping("/api/v1/users/{userId}/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(dashboardAiService.getSummary(userId).orElse(null)));
    }

    /** Regenerates the summary via Gemini — only called when the student taps "Refresh". */
    @PostMapping("/api/v1/users/{userId}/dashboard/summary/generate")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> generateSummary(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(dashboardAiService.generateSummary(userId)));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own dashboard.");
    }
}