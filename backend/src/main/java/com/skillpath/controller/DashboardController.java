package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.DashboardResponse;
import com.skillpath.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    @GetMapping("/api/v1/users/{userId}/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard(userId)));
    }
}
