package com.skillpath.service;

import com.skillpath.dto.request.AddCertificationRequest;
import com.skillpath.dto.request.AddEducationRequest;
import com.skillpath.dto.request.AddPortfolioItemRequest;
import com.skillpath.dto.response.*;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Certification.Certification;
import com.skillpath.model.Education.Education;
import com.skillpath.model.PortfolioItem.PortfolioItem;
import com.skillpath.model.Project.Project;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.User.User;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.ProjectStatus;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioItemRepository portfolioRepo;
    private final ProjectRepository       projectRepo;
    private final UserRepository          userRepo;
    private final UserSkillRepository     userSkillRepo;
    private final SkillRepository         skillRepo;
    private final ProjectMemberRepository memberRepo;
    private final CareerGoalService       goalService;
    private final CertificationRepository certRepo;
    private final ProjectRequiredSkillRepository reqSkillRepo;
    private final EducationRepository     educationRepo;

    public List<PortfolioItemResponse> getPortfolio(Long userId) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        return portfolioRepo.findByUserId(userId)
                .stream()
                .map(item -> {
                    String projectName = null;
                    if (item.getProjectId() != null) {
                        projectName = projectRepo.findById(item.getProjectId())
                                .map(p -> p.getName())
                                .orElse(null);
                    }
                    return PortfolioItemResponse.from(item, projectName);
                })
                .toList();
    }

    @Transactional
    public PortfolioItemResponse addItem(Long userId, AddPortfolioItemRequest req) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        // If a projectId is provided, verify it actually exists
        if (req.getProjectId() != null && !projectRepo.existsById(req.getProjectId()))
            throw new ResourceNotFoundException("Project not found: " + req.getProjectId());

        PortfolioItem item = PortfolioItem.builder()
                .userId(userId)
                .projectId(req.getProjectId())
                .githubUrl(req.getGithubUrl())
                .description(req.getDescription())
                .userRole(req.getUserRole())
                .build();

        PortfolioItem saved = portfolioRepo.save(item);

        String projectName = null;
        if (saved.getProjectId() != null) {
            projectName = projectRepo.findById(saved.getProjectId())
                    .map(p -> p.getName())
                    .orElse(null);
        }

        return PortfolioItemResponse.from(saved, projectName);
    }


    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        PortfolioItem item = portfolioRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException( "Portfolio item not found: " + itemId));
        if (!item.getUserId().equals(userId))
            throw new SecurityException(
                    "User " + userId + " does not own portfolio item " + itemId);

        portfolioRepo.delete(item);
    }

    @Transactional
    public void autoGenerateForCompletedProject(Long projectId, Long userId, String userRole) {
        boolean alreadyExists = portfolioRepo.findByUserId(userId).stream()
                                        .anyMatch(item -> projectId.equals(item.getProjectId()));

        if (alreadyExists) return;

        PortfolioItem item = PortfolioItem.builder()
                .userId(userId)
                .projectId(projectId)
                .userRole(userRole)
                .description("Auto-generated from completed project")
                .build();

        portfolioRepo.save(item);
    }

    @Transactional
    public CertificationResponse addCertification(Long userId, AddCertificationRequest req) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        Certification cert = Certification.builder()
                .userId(userId)
                .name(req.getName())
                .issuer(req.getIssuer())
                .credentialUrl(req.getCredentialUrl())
                .earnedOn(req.getEarnedOn())
                .build();

        return CertificationResponse.from(certRepo.save(cert));
    }

    @Transactional
    public void deleteCertification(Long userId, Long certId) {
        Certification cert = certRepo.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found: " + certId));
        if (!cert.getUserId().equals(userId))
            throw new SecurityException("User " + userId + " does not own certification " + certId);

        certRepo.delete(cert);
    }

    public List<EducationResponse> getEducation(Long userId) {
        return educationRepo.findByUserIdOrderByStartDateDesc(userId)
                .stream().map(EducationResponse::from).toList();
    }

    @Transactional
    public EducationResponse addEducation(Long userId, AddEducationRequest req) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        Education edu = Education.builder()
                .userId(userId)
                .institution(req.getInstitution())
                .degree(req.getDegree())
                .fieldOfStudy(req.getFieldOfStudy())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .description(req.getDescription())
                .build();

        return EducationResponse.from(educationRepo.save(edu));
    }

    @Transactional
    public void deleteEducation(Long userId, Long eduId) {
        Education edu = educationRepo.findById(eduId)
                .orElseThrow(() -> new ResourceNotFoundException("Education entry not found: " + eduId));
        if (!edu.getUserId().equals(userId))
            throw new SecurityException("User " + userId + " does not own education entry " + eduId);

        educationRepo.delete(edu);
    }

    /** Comma- or newline-separated free text -> a clean list, dropping blanks. */
    private List<String> parseSoftSkills(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Everything the Portfolio screen (and CV export, built from the same
     * data client-side) needs in one call: profile basics, career goal
     * progress, skills with proficiency, projects owned/joined, education,
     * and certifications.
     */
    public PortfolioResponse getSummary(Long userId, Long viewerId) {
        boolean isSelf = userId.equals(viewerId);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String careerGoalRoleName = null;
        int careerProgressPercent = 0;
        try {
            GapAnalysisResponse gap = goalService.getGapAnalysis(userId);
            careerGoalRoleName = gap.getCareerRoleName();
            careerProgressPercent = gap.getProgressPercent();
        } catch (Exception ignored) {
            // No career goal set yet - leave the defaults above.
        }

        List<SkillWithProficiencyResponse> skills = userSkillRepo.findByUserId(userId).stream()
                .map(this::toSkillWithProficiency)
                .filter(s -> s != null)
                .toList();

        List<ProjectResponse> projects = getUserProjects(userId);

        List<PortfolioItemResponse> items = getPortfolio(userId);
        List<CertificationResponse> certifications = certRepo.findByUserIdOrderByEarnedOnDesc(userId)
                .stream().map(CertificationResponse::from).toList();
        List<EducationResponse> education = getEducation(userId);

        return PortfolioResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(isSelf ? user.getPhoneNumber() : null)
                .githubUrl(user.getGithubUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .location(user.getLocation())
                .softSkills(parseSoftSkills(user.getSoftSkills()))
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .experienceLevel(user.getExperienceLevel())
                .availability(user.isAvailability())
                .memberSince(user.getCreatedAt())
                .careerGoalRoleName(careerGoalRoleName)
                .careerProgressPercent(careerProgressPercent)
                .skills(skills)
                .projects(projects)
                .portfolioItems(items)
                .certifications(certifications)
                .education(education)
                .build();
    }

    private SkillWithProficiencyResponse toSkillWithProficiency(UserSkill us) {
        Skill skill = skillRepo.findById(us.getSkillId()).orElse(null);
        if (skill == null) return null;
        return SkillWithProficiencyResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .proficiency(us.getProficiency())
                .build();
    }

    /** Owned + accepted-membership projects, deduplicated, excluding
     *  cancelled ones. Extracted out of {@link #getSummary} so
     *  PublicProfileService can build the same "Projects" list for a
     *  public profile without duplicating this logic. */
    public List<ProjectResponse> getUserProjects(Long userId) {
        Set<Long> projectIds = new LinkedHashSet<>();
        projectRepo.findByOwnerId(userId, org.springframework.data.domain.Pageable.unpaged())
                .forEach(p -> projectIds.add(p.getId()));

        memberRepo.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                .forEach(pm -> projectIds.add(pm.getProjectId()));

        return projectRepo.findAllById(projectIds).stream()
                .filter(p -> p.getStatus() != ProjectStatus.CANCELLED)
                .map(this::toProjectResponse)
                .toList();
    }

    private ProjectResponse toProjectResponse(Project p) {
        ProjectResponse resp = ProjectResponse.from(p);
        List<SkillResponse> skills = reqSkillRepo.findByProjectId(p.getId()).stream()
                .map(rs -> skillRepo.findById(rs.getSkillId()).map(SkillResponse::from).orElse(null))
                .filter(s -> s != null)
                .toList();
        resp.setRequiredSkills(skills);
        userRepo.findById(p.getOwnerId()).ifPresent(owner -> {
            resp.setOwnerName(owner.getName());
            resp.setOwnerAvatarUrl(owner.getAvatarUrl());
        });
        return resp;
    }
}