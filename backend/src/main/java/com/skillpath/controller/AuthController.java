package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.security.FirebasePrincipal;
import com.skillpath.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<UserResponse>> sync(Authentication auth) {
        FirebasePrincipal p = (FirebasePrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(authService.syncUser(p.getUid(), p.getEmail())));
    }
}