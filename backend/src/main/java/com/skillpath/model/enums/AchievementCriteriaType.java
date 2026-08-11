package com.skillpath.model.enums;

/** What an achievement's unlock condition is measured against.
 *  Paired with a numeric threshold (Achievement.criteriaValue). */
public enum AchievementCriteriaType {
    ROADMAP_STEPS_COMPLETED,
    ROADMAP_PERCENT_COMPLETE,
    SKILL_CHECKS_PASSED,
    STREAK_DAYS,
    PROJECTS_JOINED,
    PROJECTS_CREATED,
    TUTOR_MESSAGES_SENT
}