package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.AddSkillRequest;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;
    @GetMapping("/api/v1/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> list(@RequestParam(required = false) String q) {
        var result = (q == null || q.isBlank()) ? skillService.findAll() : skillService.search(q);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    @GetMapping("/api/v1/users/{userId}/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> userSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.getUserSkills(userId)));
    }
    @PostMapping("/api/v1/users/{userId}/skills")
    public ResponseEntity<ApiResponse<Void>> addSkill( @PathVariable Long userId, @Valid @RequestBody AddSkillRequest req){
        skillService.addSkillToUser(userId, req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    @DeleteMapping("/api/v1/users/{userId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@PathVariable Long userId, @PathVariable Long skillId) {
        skillService.removeSkillFromUser(userId, skillId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}