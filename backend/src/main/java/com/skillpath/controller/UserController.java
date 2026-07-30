package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.UpdateProfileRequest;
import com.skillpath.dto.response.ProjectResponse;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.service.ProjectService;
import com.skillpath.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id,req)));
    }
    @GetMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> ownedProjects(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getOwnedProjects(id, PageRequest.of(page, size))));
    }
}