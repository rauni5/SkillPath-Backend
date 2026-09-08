package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.SendChatMessageRequest;
import com.skillpath.dto.response.ChatMessageResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.TutorChatService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/users/{userId}/skills/{skillId}/chat") @RequiredArgsConstructor
public class ChatController {
    private final TutorChatService chatService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> history(
            @PathVariable Long userId, @PathVariable Long skillId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.getHistory(userId, skillId)));
    }

    @PostMapping("/intro")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> intro(
            @PathVariable Long userId, @PathVariable Long skillId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.getIntro(userId, skillId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(
            @PathVariable Long userId, @PathVariable Long skillId,
            @Valid @RequestBody SendChatMessageRequest req, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.sendMessage(userId, skillId, req.getMessage())));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own chat.");
    }
}