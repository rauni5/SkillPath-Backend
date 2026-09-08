package com.skillpath.service;

import com.skillpath.dto.response.*;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.User.User;
import com.skillpath.model.UserSkill.UserSkill;
import com.skillpath.repository.CertificationRepository;
import com.skillpath.repository.SkillRepository;
import com.skillpath.repository.UserRepository;
import com.skillpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Public, shareable profile links — a scoped-down cousin of
 * {@link PortfolioService}. That service builds the profile view for
 * another *authenticated* SkillPath user; this one builds the view for a
 * completely anonymous visitor with no account, so it's deliberately more
 * conservative about what it exposes (see {@link PublicProfileResponse}'s
 * doc comment).
 *
 * Off by default. A user opts in from Settings, which is when a token
 * first gets generated; the token then persists across enable/disable so
 * toggling sharing off and back on doesn't invalidate a link someone
 * already has, without the user re-sharing anything.
 */
@Service
@RequiredArgsConstructor
public class PublicProfileService {

    private final UserRepository userRepo;
    private final UserSkillRepository userSkillRepo;
    private final SkillRepository skillRepo;
    private final CertificationRepository certRepo;
    private final PortfolioService portfolioService;
    private final GamificationService gamificationService;
    private final CareerGoalService careerGoalService;

    public PublicProfileSettingsResponse getSettings(Long userId) {
        User user = requireUser(userId);
        return toSettings(user);
    }

    @Transactional
    public PublicProfileSettingsResponse enable(Long userId) {
        User user = requireUser(userId);
        if (user.getPublicProfileToken() == null) {
            user.setPublicProfileToken(generateToken());
        }
        user.setPublicProfileEnabled(true);
        userRepo.save(user);
        return toSettings(user);
    }

    @Transactional
    public PublicProfileSettingsResponse disable(Long userId) {
        User user = requireUser(userId);
        user.setPublicProfileEnabled(false);
        userRepo.save(user);
        return toSettings(user);
    }

    /** Invalidates the old link immediately — anyone who had it gets a 404
     *  from that point on, matching the "disabled" behavior. */
    @Transactional
    public PublicProfileSettingsResponse regenerate(Long userId) {
        User user = requireUser(userId);
        user.setPublicProfileToken(generateToken());
        userRepo.save(user);
        return toSettings(user);
    }

    /** Deliberately returns the same 404 whether the token never existed,
     *  belongs to a disabled profile, or belongs to a deactivated account —
     *  a visitor can't distinguish those cases from the outside. */
    public PublicProfileResponse getPublicProfile(String token) {
        User user = userRepo
                .findByPublicProfileTokenAndPublicProfileEnabledTrue(token)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("No public profile at this link."));

        Long userId = user.getId();

        String careerGoalRoleName = null;
        int careerProgressPercent = 0;
        try {
            GapAnalysisResponse gap = careerGoalService.getGapAnalysis(userId);
            careerGoalRoleName = gap.getCareerRoleName();
            careerProgressPercent = gap.getProgressPercent();
        } catch (Exception ignored) {
            // No career goal set yet - leave the defaults above.
        }

        List<SkillWithProficiencyResponse> skills = userSkillRepo.findByUserId(userId).stream()
                .map(this::toSkillWithProficiency)
                .filter(s -> s != null)
                .toList();

        List<AchievementResponse> achievements = gamificationService.getAchievements(userId).stream()
                .filter(AchievementResponse::isUnlocked)
                .toList();

        List<CertificationResponse> certifications = certRepo.findByUserIdOrderByEarnedOnDesc(userId)
                .stream().map(CertificationResponse::from).toList();

        return PublicProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .softSkills(parseSoftSkills(user.getSoftSkills()))
                .experienceLevel(user.getExperienceLevel())
                .availability(user.isAvailability())
                .githubUrl(user.getGithubUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .memberSince(user.getCreatedAt())
                .careerGoalRoleName(careerGoalRoleName)
                .careerProgressPercent(careerProgressPercent)
                .skills(skills)
                .achievements(achievements)
                .projects(portfolioService.getUserProjects(userId))
                .portfolioItems(portfolioService.getPortfolio(userId))
                .certifications(certifications)
                .education(portfolioService.getEducation(userId))
                .build();
    }

    private User requireUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private PublicProfileSettingsResponse toSettings(User user) {
        return PublicProfileSettingsResponse.builder()
                .enabled(user.isPublicProfileEnabled())
                .token(user.getPublicProfileToken())
                .build();
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
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

    /** Comma- or newline-separated free text -> a clean list, dropping blanks. */
    private List<String> parseSoftSkills(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
