package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_achievements_user_achievement", columnNames = { "user_id",
                                "achievement_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "achievement_id", nullable = false)
        private Achievement achievement;

        @Column(name = "is_unlocked", nullable = false)
        @Builder.Default
        private Boolean isUnlocked = false;

        @Column(name = "unlocked_at")
        private LocalDateTime unlockedAt;

        @Column(name = "current_progress", nullable = false)
        @Builder.Default
        private Integer currentProgress = 0;
}