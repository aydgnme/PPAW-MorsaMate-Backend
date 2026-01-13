package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AchievementRarity rarity = AchievementRarity.COMMON;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 10;

    @Column(name = "gem_reward", nullable = false)
    @Builder.Default
    private Integer gemReward = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false)
    private CriteriaType criteriaType;

    @Column(name = "criteria_target", nullable = false)
    @Builder.Default
    private Integer criteriaTarget = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria_metadata", columnDefinition = "jsonb")
    private Map<String, Object> criteriaMetadata;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum AchievementCategory {
        PROGRESS, STREAK, SOCIAL, PERFORMANCE, HIDDEN
    }

    public enum AchievementRarity {
        COMMON, RARE, EPIC, LEGENDARY
    }

    public enum CriteriaType {
        LESSONS_COMPLETED,
        STREAK_DAYS,
        PERFECT_SCORE,
        TOTAL_POINTS,
        USE_HEARTS,
        EXERCISES_COMPLETED
    }
}