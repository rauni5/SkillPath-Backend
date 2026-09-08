package com.skillpath.service;
import com.skillpath.dto.response.AchievementResponse;
import com.skillpath.dto.response.StreakResponse;
import com.skillpath.model.Achievement.Achievement;
import com.skillpath.model.ChatMessage.ChatMessage;
import com.skillpath.model.ProjectComment.ProjectComment;
import com.skillpath.model.ProjectMember.ProjectMember;
import com.skillpath.model.RoadmapStep.RoadmapStep;
import com.skillpath.model.SkillCheckAttempt.SkillCheckAttempt;
import com.skillpath.model.UserAchievement.UserAchievement;
import com.skillpath.model.UserStreak.UserStreak;
import com.skillpath.model.enums.MemberStatus;
import com.skillpath.model.enums.MessageRole;
import com.skillpath.model.enums.SkillCheckStatus;
import com.skillpath.model.enums.StepStatus;
import com.skillpath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes and unlocks achievements, and tracks the user's daily activity
 * streak. Everything here is derived from data that already exists elsewhere
 * (roadmap completions, passed skill checks, project membership, tutor chat
 * activity, and project discussion comments) — evaluated lazily whenever the
 * achievements/streak screens are opened, with newly-met achievements
 * persisted permanently at that point.
 */
@Service @RequiredArgsConstructor
public class GamificationService {
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AchievementRepository achievementRepo;
    private final UserAchievementRepository userAchievementRepo;
    private final UserStreakRepository streakRepo;
    private final RoadmapStepRepository stepRepo;
    private final SkillCheckAttemptRepository attemptRepo;
    private final ProjectMemberRepository memberRepo;
    private final ProjectRepository projectRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ProjectCommentRepository projectCommentRepo;

    @Transactional
    public List<AchievementResponse> getAchievements(Long userId) {
        ProgressStats stats = computeStats(userId);
        StreakResponse streak = computeAndPersistStreak(userId);

        List<Achievement> catalog = achievementRepo.findAll();
        Set<Long> unlockedIds = userAchievementRepo.findByUserId(userId).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());
        Map<Long, Instant> unlockedAtById = userAchievementRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, UserAchievement::getUnlockedAt));

        List<AchievementResponse> result = new ArrayList<>();
        for (Achievement a : catalog) {
            boolean alreadyUnlocked = unlockedIds.contains(a.getId());

            // A retired (disabled) achievement nobody has earned yet is
            // no longer obtainable — hide it entirely rather than show a
            // badge nobody can ever unlock.
            if (!a.isEnabled() && !alreadyUnlocked) continue;

            boolean meetsCriteria = alreadyUnlocked
                    || (a.isEnabled() && meetsCriteria(a, stats, streak));

            if (meetsCriteria && !alreadyUnlocked) {
                userAchievementRepo.save(UserAchievement.builder()
                        .userId(userId).achievementId(a.getId()).build());
                unlockedAtById.put(a.getId(), Instant.now());
            }

            result.add(AchievementResponse.builder()
                    .code(a.getCode())
                    .title(a.getTitle())
                    .description(a.getDescription())
                    .icon(a.getIcon())
                    .category(a.getCategory())
                    .unlocked(meetsCriteria)
                    .unlockedAt(unlockedAtById.get(a.getId()))
                    .criteriaType(a.getCriteriaType())
                    .criteriaValue(a.getCriteriaValue())
                    .build());
        }
        return result;
    }

    @Transactional
    public StreakResponse getStreak(Long userId) {
        return computeAndPersistStreak(userId);
    }

    /** Generic rule evaluation — works the same for seeded and
     *  admin-created achievements alike, since both are just a
     *  (criteriaType, criteriaValue) pair now. */
    private boolean meetsCriteria(Achievement a, ProgressStats s, StreakResponse streak) {
        int value = a.getCriteriaValue();
        return switch (a.getCriteriaType()) {
            case ROADMAP_STEPS_COMPLETED -> s.completedSteps() >= value;
            case ROADMAP_PERCENT_COMPLETE -> s.totalSteps() > 0 && s.completedSteps() * 100 >= s.totalSteps() * value;
            case SKILL_CHECKS_PASSED -> s.skillChecksPassed() >= value;
            case STREAK_DAYS -> streak.getLongestStreak() >= value;
            case PROJECTS_JOINED -> s.projectsJoined() >= value;
            case PROJECTS_CREATED -> s.projectsCreated() >= value;
            case TUTOR_MESSAGES_SENT -> s.tutorMessagesSent() >= value;
        };
    }

    private ProgressStats computeStats(Long userId) {
        List<RoadmapStep> steps = stepRepo.findByUserIdOrderByStepOrder(userId);
        long completedSteps = steps.stream().filter(st -> st.getStatus() == StepStatus.DONE).count();

        long skillChecksPassed = attemptRepo.countByUserIdAndStatusAndProficiencyIsNotNull(
                userId, SkillCheckStatus.SUBMITTED);

        long projectsJoined = memberRepo.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED).size();
        long projectsCreated = projectRepo.countByOwnerId(userId);

        long tutorMessagesSent = chatMessageRepo.countByUserIdAndRole(userId, MessageRole.USER);

        return new ProgressStats(
                (int) completedSteps, steps.size(), (int) skillChecksPassed,
                (int) projectsJoined, (int) projectsCreated, (int) tutorMessagesSent);
    }

    /**
     * A day counts toward the streak if the user did *any* of these on it:
     * completed a roadmap step, passed a skill check, sent a tutor chat
     * message, posted a project discussion comment, or joined a project.
     * Previously only the first two counted — tutor chat and project
     * activity are common ways someone genuinely engages with the app on a
     * given day and were unfairly excluded.
     */
    private StreakResponse computeAndPersistStreak(Long userId) {
        Set<LocalDate> activeDates = new TreeSet<>();

        stepRepo.findByUserIdOrderByStepOrder(userId).stream()
                .filter(s -> s.getStatus() == StepStatus.DONE && s.getCompletedAt() != null)
                .forEach(s -> activeDates.add(s.getCompletedAt().atZone(ZONE).toLocalDate()));

        attemptRepo.findByUserIdAndStatusAndProficiencyIsNotNull(userId, SkillCheckStatus.SUBMITTED).stream()
                .map(SkillCheckAttempt::getSubmittedAt)
                .filter(Objects::nonNull)
                .forEach(t -> activeDates.add(t.atZone(ZONE).toLocalDate()));

        chatMessageRepo.findByUserIdAndRole(userId, MessageRole.USER).stream()
                .map(ChatMessage::getCreatedAt)
                .filter(Objects::nonNull)
                .forEach(t -> activeDates.add(t.atZone(ZONE).toLocalDate()));

        projectCommentRepo.findByAuthorId(userId).stream()
                .map(ProjectComment::getCreatedAt)
                .filter(Objects::nonNull)
                .forEach(t -> activeDates.add(t.atZone(ZONE).toLocalDate()));

        memberRepo.findByUserId(userId).stream()
                .map(ProjectMember::getJoinedAt)
                .filter(Objects::nonNull)
                .forEach(t -> activeDates.add(t.atZone(ZONE).toLocalDate()));

        int current = 0, longest = 0;
        LocalDate lastActivity = null;

        if (!activeDates.isEmpty()) {
            List<LocalDate> sorted = new ArrayList<>(activeDates);
            int run = 1;
            longest = 1;
            for (int i = 1; i < sorted.size(); i++) {
                run = sorted.get(i).equals(sorted.get(i - 1).plusDays(1)) ? run + 1 : 1;
                longest = Math.max(longest, run);
            }
            lastActivity = sorted.get(sorted.size() - 1);
            LocalDate today = LocalDate.now(ZONE);
            if (lastActivity.isBefore(today.minusDays(1))) {
                current = 0;
            } else {
                current = 1;
                for (int i = sorted.size() - 2; i >= 0; i--) {
                    if (sorted.get(i).equals(sorted.get(i + 1).minusDays(1))) current++;
                    else break;
                }
            }
        }

        UserStreak record = streakRepo.findByUserId(userId).orElseGet(() ->
                UserStreak.builder().userId(userId).currentStreak(0).longestStreak(0).build());
        record.setCurrentStreak(current);
        record.setLongestStreak(Math.max(longest, record.getLongestStreak()));
        record.setLastActivityDate(lastActivity);
        streakRepo.save(record);

        return StreakResponse.builder()
                .currentStreak(record.getCurrentStreak())
                .longestStreak(record.getLongestStreak())
                .lastActivityDate(record.getLastActivityDate())
                .build();
    }

    private record ProgressStats(
            int completedSteps, int totalSteps, int skillChecksPassed,
            int projectsJoined, int projectsCreated, int tutorMessagesSent) {
    }
}