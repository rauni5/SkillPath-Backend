package com.skillpath.service;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Project.Project;
import com.skillpath.model.ProjectMember.ProjectMember;
import com.skillpath.model.ProjectMember.ProjectMemberId;
import com.skillpath.model.ProjectRequiredSkill.ProjectRequiredSkill;
import com.skillpath.model.enums.*;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepo;
    private final ProjectRequiredSkillRepository reqSkillRepo;
    private final ProjectMemberRepository memberRepo;
    private final SkillRepository skillRepo;
    private final PortfolioService portfolioService;
    public Page<ProjectResponse> browseOpen(Pageable pageable) {
        return projectRepo.findByStatus(ProjectStatus.OPEN, pageable).map(this::enrich);
    }
    public ProjectResponse getById(Long id) {
        return enrich(projectRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found:"+id)));
    }
    @Transactional
    public ProjectResponse create(Long ownerId, CreateProjectRequest req) {
        Project p = projectRepo.save(Project.builder()
                                .name(req.getName())
                                .description(req.getDescription())
                                .difficulty(req.getDifficulty())
                                .teamSize(req.getTeamSize())
                                .ownerId(ownerId)
                                .status(ProjectStatus.OPEN).build());
        if (req.getRequiredSkillIds() != null)
            req.getRequiredSkillIds().forEach(sid -> reqSkillRepo.save(ProjectRequiredSkill.builder().projectId(p.getId()).skillId(sid).build()));
        return enrich(p);
    }
    @Transactional
    public void requestJoin(Long projectId, Long userId) {
        if (memberRepo.existsByProjectIdAndUserId(projectId, userId)) throw new IllegalStateException("Already requested or joined");
        memberRepo.save(ProjectMember.builder()
                            .projectId(projectId)
                            .userId(userId)
                            .status(MemberStatus.PENDING).build());
    }
    @Transactional
    public void updateMemberStatus(Long projectId, Long userId, UpdateMemberStatusRequest req) {
        ProjectMember m = memberRepo.findById(new ProjectMemberId(projectId, userId))
                                        .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        m.setStatus(req.getStatus());
        memberRepo.save(m);
    }
    private ProjectResponse enrich(Project p) {
        List<SkillResponse> skills = reqSkillRepo.findByProjectId(p.getId())
                                                .stream().map(rs -> skillRepo.findById(rs.getSkillId())
                                                .map(SkillResponse::from).orElseThrow()).toList();
        ProjectResponse resp = ProjectResponse.from(p);
        resp.setRequiredSkills(skills);
        return resp;
    }
    @Transactional
    public ProjectResponse completeProject(Long projectId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));
    
        project.setStatus(ProjectStatus.COMPLETED);
        projectRepo.save(project);
    
        // Auto-generate portfolio entries for all accepted members
        memberRepo.findByProjectIdAndStatus(projectId, MemberStatus.ACCEPTED)
                .forEach(member ->
                    portfolioService.autoGenerateForCompletedProject(
                        projectId,
                        member.getUserId(),
                        member.getRole()
                    )
                );
    
        // Also create one for the project owner
        portfolioService.autoGenerateForCompletedProject(
            projectId,
            project.getOwnerId(),
            "Project Owner"
        );
    
        return enrich(project);
    }
}
