package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.SendChatMessageRequest;
import com.skillpath.dto.response.RoadmapChatMessageResponse;
import com.skillpath.dto.response.RoadmapChatSessionResponse;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.RoadmapChatService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/users/{userId}/roadmap-chat") @RequiredArgsConstructor
public class RoadmapChatController {
    private final RoadmapChatService chatService;
    private final UserService userService;

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<RoadmapChatSessionResponse>>> listSessions(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.listSessions(userId)));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<RoadmapChatSessionResponse>> createSession(
            @PathVariable Long userId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.createSession(userId)));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<RoadmapChatMessageResponse>>> messages(
            @PathVariable Long userId, @PathVariable Long sessionId, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.getMessages(userId, sessionId)));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<RoadmapChatMessageResponse>> send(
            @PathVariable Long userId, @PathVariable Long sessionId,
            @Valid @RequestBody SendChatMessageRequest req, Authentication auth) {
        requireSelf(userId, auth);
        return ResponseEntity.ok(ApiResponse.ok(chatService.sendMessage(userId, sessionId, req.getMessage())));
    }

    private void requireSelf(Long pathUserId, Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        Long callerId = userService.getEntityByFirebaseUid(p.getUid()).getId();
        if (!callerId.equals(pathUserId))
            throw new ForbiddenException("You can only access your own chats.");
    }
}