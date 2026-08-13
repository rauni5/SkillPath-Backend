package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.AchievementResponse;
import com.skillpath.dto.response.StreakResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.GamificationService;
import com.skillpath.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/users/{userId}/gamification") @RequiredArgsConstructor
public class GamificationController {
    private final GamificationService gamificationService;
    private final UserService userService;

    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> achievements(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getAchievements(userId)));
    }

    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<StreakResponse>> streak(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getStreak(userId)));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own achievements.");
    }
}