package com.skillpath.model.Achievement;
import com.skillpath.model.enums.AchievementCriteriaType;
import jakarta.persistence.*;
import lombok.*;

/** Catalog of unlockable achievements. Admin-manageable — seeded initially
 *  via migration, then created/edited/retired through the admin panel. */
@Entity @Table(name = "achievements")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Achievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false, length = 50) private String code;
    @Column(nullable = false, length = 100) private String title;
    @Column(nullable = false, length = 255) private String description;
    @Column(nullable = false, length = 50) private String icon;
    @Column(nullable = false, length = 30) private String category;

    /** What stat this achievement is measured against (e.g. roadmap steps
     *  completed), paired with the numeric threshold in criteriaValue. */
    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 40)
    private AchievementCriteriaType criteriaType;

    @Column(name = "criteria_value", nullable = false)
    private int criteriaValue;

    /** Retired achievements are hidden from users who haven't earned them
     *  yet, but stay visible to anyone who already unlocked one before it
     *  was disabled — so nobody loses a badge they've earned. */
    @Column(nullable = false) @Builder.Default
    private boolean enabled = true;
}