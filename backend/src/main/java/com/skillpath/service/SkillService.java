package com.skillpath.service;
import com.skillpath.algorithm.graph.SkillGraph;
import com.skillpath.dto.request.AddSkillRequest;
import com.skillpath.dto.response.SkillResponse;
import com.skillpath.dto.response.SkillWithProficiencyResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.model.UserSkill.UserSkillId;
import com.skillpath.repository.SkillDependencyRepository;
import com.skillpath.repository.SkillRepository;
import com.skillpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepo;
    private final UserSkillRepository userSkillRepo;
    private final SkillDependencyRepository depRepo;
    public List<SkillResponse> findAll() {
        return skillRepo.findAll().stream().map(SkillResponse::from).toList();
    }
    // Basic DB LIKE — replaced with Trie autocomplete in Phase 13
    public List<SkillResponse> search(String prefix) {
        return skillRepo.findByNameContainingIgnoreCase(prefix)
                .stream().map(SkillResponse::from).toList();
    }
    public List<SkillWithProficiencyResponse> getUserSkills(Long userId) {
        return userSkillRepo.findByUserId(userId).stream()
                    .map(us -> skillRepo.findById(us.getSkillId())
                    .map(skill -> SkillWithProficiencyResponse.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .proficiency(us.getProficiency())
                        .build())
                    .orElseThrow())
                .toList();
    }
    @Transactional
    public void addSkillToUser(Long userId, AddSkillRequest req) {
        if (!skillRepo.existsById(req.getSkillId())) throw new ResourceNotFoundException("Skill not found:"+req.getSkillId());
        userSkillRepo.save(UserSkill.builder()
                    .userId(userId).skillId(req.getSkillId())
                    .proficiency(req.getProficiency()).build());
        SkillGraph graph = new SkillGraph();
        depRepo.findAllDependencies().forEach(dep -> {
            graph.addSkill(dep.getSkillId());
            graph.addSkill(dep.getPrerequisiteId());
            graph.addDependency(dep.getSkillId(), dep.getPrerequisiteId());
        });
        for (Long prereqId : graph.getAllPrerequisites(req.getSkillId())) {
            var id = new UserSkillId(userId, prereqId);
            if (!userSkillRepo.existsById(id)) {
                userSkillRepo.save(UserSkill.builder()
                        .userId(userId).skillId(prereqId)
                        .proficiency(req.getProficiency()).build());
            }
        }
    }
    @Transactional
    public void removeSkillFromUser(Long userId, Long skillId) {
        userSkillRepo.deleteByUserIdAndSkillId(userId, skillId);
    }
}