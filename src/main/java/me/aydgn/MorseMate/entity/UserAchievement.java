package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_achievements_user_achievement", columnNames = {"user_id","achievement_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement extends BaseEntity {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "user_achievements_user_id_fkey")
    )
    private User user;

    // achievement_id INTEGER REFERENCES achievements(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "achievement_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "user_achievements_achievement_id_fkey")
    )
    private Achievement achievement;

    // earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    @PrePersist
    private void prePersist() {
        if (this.earnedAt == null) this.earnedAt = LocalDateTime.now();
    }
}