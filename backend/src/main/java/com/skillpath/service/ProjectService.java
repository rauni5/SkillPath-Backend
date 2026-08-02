package com.skillpath.service;
import com.skillpath.dto.request.*;
import com.skillpath.dto.response.*;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Project.Project;
import com.skillpath.model.ProjectMember.ProjectMember;
import com.skillpath.model.ProjectMember.ProjectMemberId;
import com.skillpath.model.ProjectRequiredRole.ProjectRequiredRole;
import com.skillpath.model.ProjectRequiredSkill.ProjectRequiredSkill;
import com.skillpath.model.User.User;
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
    private final ProjectRequiredRoleRepository reqRoleRepo;
    private final CareerRoleRepository careerRoleRepo;
    private final ProjectMemberRepository memberRepo;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;
    private final PortfolioService portfolioService;
    public Page<ProjectResponse> browseOpen(Pageable pageable) {
        return projectRepo.findByStatus(ProjectStatus.OPEN, pageable).map(this::enrich);
    }
    public Page<ProjectResponse> search(String difficulty, List<Long> skillIds, List<Long> roleIds, String q, Pageable pageable) {
        String normalizedDifficulty = (difficulty == null || difficulty.isBlank())
                ? null : difficulty.trim().toUpperCase();
        List<Long> normalizedSkillIds = (skillIds == null || skillIds.isEmpty())
                ? null : skillIds;
        List<Long> normalizedRoleIds = (roleIds == null || roleIds.isEmpty())
                ? null : roleIds;
        String normalizedQ = (q == null || q.isBlank()) ? null : q.trim();
        return projectRepo.search(ProjectStatus.OPEN, normalizedDifficulty, normalizedSkillIds, normalizedRoleIds, normalizedQ, pageable)
                .map(this::enrich);
    }
    public ProjectResponse getById(Long id) {
        return enrich(projectRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found:"+id)));
    }
    /** Like getById, but also fills in the viewer's own membership status (pending/accepted/rejected/none). */
    public ProjectResponse getById(Long id, Long viewerId) {
        ProjectResponse resp = getById(id);
        memberRepo.findById(new ProjectMemberId(id, viewerId))
                .ifPresent(m -> {
                    resp.setViewerMembershipStatus(m.getStatus());
                    resp.setViewerInvitedByOwner(m.isInvitedByOwner());
                });
        return resp;
    }
    public Page<ProjectResponse> getOwnedProjects(Long ownerId, Pageable pageable) {
        return projectRepo.findByOwnerId(ownerId, pageable).map(this::enrich);
    }
    public void assertOwner(Long projectId, Long requesterId) {
        requireOwner(projectId, requesterId);
    }
    private Project requireOwner(Long projectId, Long requesterId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found:"+projectId));
        if (!project.getOwnerId().equals(requesterId))
            throw new ForbiddenException("Only the project owner can do this.");
        return project;
    }
    public List<ProjectMemberResponse> getMembers(Long projectId, Long requesterId) {
        Project project = requireOwner(projectId, requesterId);
        return memberRepo.findByProjectId(projectId).stream()
                .filter(m -> !m.getUserId().equals(project.getOwnerId()))
                .map(this::toMemberResponse)
                .toList();
    }
    /** Team roster (owner + accepted members) — visible only to the owner or
     * an accepted member of the project, never the general public. Emails
     * are only included when the viewer is the owner. */
    public List<ProjectMemberResponse> getTeam(Long projectId, Long requesterId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found:"+projectId));
        boolean isOwner = project.getOwnerId().equals(requesterId);
        boolean isAcceptedMember = memberRepo.findById(new ProjectMemberId(projectId, requesterId))
                .map(m -> m.getStatus() == MemberStatus.ACCEPTED).orElse(false);
        if (!isOwner && !isAcceptedMember)
            throw new ForbiddenException("Only project members can view the team.");

        User owner = userRepo.findById(project.getOwnerId()).orElseThrow();
        List<ProjectMemberResponse> team = new java.util.ArrayList<>();
        team.add(ProjectMemberResponse.builder()
                .userId(owner.getId()).name(owner.getName()).email(owner.getEmail())
                .avatarUrl(owner.getAvatarUrl()).status(MemberStatus.ACCEPTED).role("Owner")
                .invitedByOwner(false)
                .build());
        memberRepo.findByProjectIdAndStatus(projectId, MemberStatus.ACCEPTED).stream()
                .filter(m -> !m.getUserId().equals(project.getOwnerId()))
                .forEach(m -> team.add(toMemberResponse(m)));

        List<ProjectMemberResponse> visibleTeam = team;
        if (!isOwner) {
            visibleTeam = team.stream()
                    .map(m -> ProjectMemberResponse.builder()
                            .userId(m.getUserId()).name(m.getName()).email(null)
                            .avatarUrl(m.getAvatarUrl()).status(m.getStatus()).role(m.getRole())
                            .invitedByOwner(m.isInvitedByOwner())
                            .build())
                    .toList();
        }
        return visibleTeam;
    }
    private ProjectMemberResponse toMemberResponse(ProjectMember m) {
        User u = userRepo.findById(m.getUserId()).orElseThrow();
        return ProjectMemberResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .status(m.getStatus())
                .role(m.getRole())
                .invitedByOwner(m.isInvitedByOwner())
                .build();
    }
    @Transactional
    public void removeMember(Long projectId, Long requesterId, Long targetUserId) {
        Project project = requireOwner(projectId, requesterId);
        if (targetUserId.equals(project.getOwnerId()))
            throw new IllegalStateException("The project owner can't be removed.");
        memberRepo.deleteById(new ProjectMemberId(projectId, targetUserId));
    }
    @Transactional
    public ProjectResponse create(Long ownerId, CreateProjectRequest req) {
        Project p = projectRepo.save(Project.builder()
                                .name(req.getName())
                                .description(req.getDescription())
                                .difficulty(req.getDifficulty())
                                .link(req.getLink())
                                .teamSize(req.getTeamSize())
                                .ownerId(ownerId)
                                .status(ProjectStatus.OPEN).build());
        if (req.getRequiredSkillIds() != null)
            req.getRequiredSkillIds().forEach(sid -> reqSkillRepo.save(ProjectRequiredSkill.builder().projectId(p.getId()).skillId(sid).build()));
        if (req.getRequiredRoleIds() != null)
            req.getRequiredRoleIds().forEach(rid -> reqRoleRepo.save(ProjectRequiredRole.builder().projectId(p.getId()).roleId(rid).build()));
        // The owner is a member of their own project from the start.
        memberRepo.save(ProjectMember.builder()
                            .projectId(p.getId())
                            .userId(ownerId)
                            .status(MemberStatus.ACCEPTED)
                            .role("Owner")
                            .invitedByOwner(false).build());
        return enrich(p);
    }
    @Transactional
    public ProjectResponse update(Long projectId, Long requesterId, UpdateProjectRequest req) {
        Project p = requireOwner(projectId, requesterId);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setDifficulty(req.getDifficulty());
        p.setLink(req.getLink());
        p.setTeamSize(req.getTeamSize());
        projectRepo.save(p);

        reqSkillRepo.deleteByProjectId(projectId);
        if (req.getRequiredSkillIds() != null)
            req.getRequiredSkillIds().forEach(sid -> reqSkillRepo.save(ProjectRequiredSkill.builder().projectId(projectId).skillId(sid).build()));

        reqRoleRepo.deleteByProjectId(projectId);
        if (req.getRequiredRoleIds() != null)
            req.getRequiredRoleIds().forEach(rid -> reqRoleRepo.save(ProjectRequiredRole.builder().projectId(projectId).roleId(rid).build()));

        return enrich(p);
    }
    @Transactional
    public void requestJoin(Long projectId, Long userId) {
        if (memberRepo.existsByProjectIdAndUserId(projectId, userId)) throw new IllegalStateException("Already requested or joined");
        memberRepo.save(ProjectMember.builder()
                            .projectId(projectId)
                            .userId(userId)
                            .status(MemberStatus.PENDING)
                            .invitedByOwner(false).build());
    }
    @Transactional
    public void updateMemberStatus(Long projectId, Long requesterId, Long userId, UpdateMemberStatusRequest req) {
        requireOwner(projectId, requesterId);
        ProjectMember m = memberRepo.findById(new ProjectMemberId(projectId, userId))
                                        .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        m.setStatus(req.getStatus());
        memberRepo.save(m);
    }
    // --- Owner-initiated invites ---
    @Transactional
    public void inviteMember(Long projectId, Long ownerId, Long targetUserId) {
        requireOwner(projectId, ownerId);
        if (memberRepo.existsByProjectIdAndUserId(projectId, targetUserId))
            throw new IllegalStateException("This person already has a pending request, invite, or membership on this project.");
        memberRepo.save(ProjectMember.builder()
                            .projectId(projectId)
                            .userId(targetUserId)
                            .status(MemberStatus.PENDING)
                            .invitedByOwner(true).build());
    }
    public List<ProjectInviteResponse> getMyInvites(Long userId) {
        return memberRepo.findByUserId(userId).stream()
                .filter(m -> m.isInvitedByOwner() && m.getStatus() == MemberStatus.PENDING)
                .map(m -> {
                    Project p = projectRepo.findById(m.getProjectId()).orElseThrow();
                    return ProjectInviteResponse.builder()
                            .projectId(p.getId())
                            .projectName(p.getName())
                            .difficulty(p.getDifficulty())
                            .teamSize(p.getTeamSize())
                            .build();
                })
                .toList();
    }
    @Transactional
    public void respondToInvite(Long userId, Long projectId, UpdateMemberStatusRequest req) {
        ProjectMember m = memberRepo.findById(new ProjectMemberId(projectId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));
        if (!m.isInvitedByOwner())
            throw new ForbiddenException("This isn't an invite you can respond to.");
        if (!m.getUserId().equals(userId))
            throw new ForbiddenException("You can only respond to your own invites.");
        m.setStatus(req.getStatus());
        memberRepo.save(m);
    }
    // --- Status-change alerts ---
    /** All of this user's own join requests/invites (any status) — used client-side
     * to detect accept/reject changes since the last time they checked. */
    public List<MembershipStatusResponse> getMyMemberships(Long userId) {
        return memberRepo.findByUserId(userId).stream()
                .map(m -> {
                    Project p = projectRepo.findById(m.getProjectId()).orElseThrow();
                    return MembershipStatusResponse.builder()
                            .projectId(p.getId())
                            .projectName(p.getName())
                            .status(m.getStatus())
                            .build();
                })
                .toList();
    }
    private ProjectResponse enrich(Project p) {
        List<SkillResponse> skills = reqSkillRepo.findByProjectId(p.getId())
                                                .stream().map(rs -> skillRepo.findById(rs.getSkillId())
                                                .map(SkillResponse::from).orElseThrow()).toList();
        var roles = reqRoleRepo.findByProjectId(p.getId())
                                                .stream().map(rr -> careerRoleRepo.findById(rr.getRoleId())
                                                .orElseThrow()).toList();
        ProjectResponse resp = ProjectResponse.from(p);
        resp.setRequiredSkills(skills);
        resp.setRequiredRoles(roles);
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
