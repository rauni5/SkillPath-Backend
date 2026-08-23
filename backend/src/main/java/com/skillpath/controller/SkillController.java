package com.skillpath.controller;
import com.skillpath.dto.ApiResponse;
import com.skillpath.dto.request.AddSkillRequest;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.dto.response.SkillWithProficiencyResponse;
import com.skillpath.repository.SkillRepository;
import com.skillpath.service.SkillService;
import com.skillpath.service.SkillTrieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;
    private final SkillTrieService skillTrieService;
    private final SkillRepository skillRepo;
    @GetMapping("/api/v1/skills")
    public ResponseEntity<ApiResponse<?>> list(@RequestParam(required = false) String q) {
    if (q != null && !q.isBlank()) {
        // Trie autocomplete
        List<String> names = skillTrieService.autocomplete(q);
        List<SkillResponse> result = names.stream().flatMap(name -> skillRepo
                                                                    .findByNameContainingIgnoreCase(name).stream()
                                                                    .map(SkillResponse::from))
                                                                    .distinct().toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
        }
        return ResponseEntity.ok(ApiResponse.ok(skillService.findAll()));
    }

    @GetMapping("/api/v1/users/{userId}/skills")
    public ResponseEntity<ApiResponse<List<SkillWithProficiencyResponse>>> userSkills(@PathVariable Long userId) {
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