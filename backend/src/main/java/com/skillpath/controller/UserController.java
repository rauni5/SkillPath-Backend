package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.UpdateProfileRequest;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id,req)));
    }
}
