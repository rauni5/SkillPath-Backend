package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.SubmitSkillCheckRequest;
import com.skillpath.dto.response.SkillCheckGenerateResponse;
import com.skillpath.dto.response.SkillCheckResultResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.SkillCheckService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/users/{userId}/skills/{skillId}/skill-check") @RequiredArgsConstructor
public class SkillCheckController {
    private final SkillCheckService skillCheckService;
    private final UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SkillCheckGenerateResponse>> generate(
            @PathVariable Long userId, @PathVariable Long skillId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(skillCheckService.generate(userId, skillId)));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SkillCheckResultResponse>> submit(
            @PathVariable Long userId, @PathVariable Long skillId,
            @Valid @RequestBody SubmitSkillCheckRequest req, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(
                skillCheckService.submit(userId, req.getAttemptId(), req.getAnswers())));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own skill checks.");
    }
}