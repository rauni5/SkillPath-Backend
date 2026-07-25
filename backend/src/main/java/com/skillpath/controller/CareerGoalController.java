package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.SetCareerGoalRequest;
import com.skillpath.dto.response.GapAnalysisResponse;
import com.skillpath.model.CareerRole.CareerRole;
import com.skillpath.repository.CareerRoleRepository;
import com.skillpath.service.CareerGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class CareerGoalController {
    private final CareerGoalService goalService;
    private final CareerRoleRepository roleRepo;
    @GetMapping("/api/v1/career-roles")
    public ResponseEntity<ApiResponse<List<CareerRole>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleRepo.findAll()));
    }
    @PostMapping("/api/v1/users/{userId}/career-goal")
    public ResponseEntity<ApiResponse<Void>> setGoal(@PathVariable Long userId, @Valid @RequestBody SetCareerGoalRequest req) {
        goalService.setGoal(userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @GetMapping("/api/v1/users/{userId}/career-goal/gap")
    public ResponseEntity<ApiResponse<GapAnalysisResponse>> gap(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getGapAnalysis(userId)));
    }
}
