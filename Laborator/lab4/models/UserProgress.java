package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_progress_user_lesson", columnNames = {"user_id", "lesson_id"})
        },
        indexes = {
                @Index(name = "idx_user_progress_user", columnList = "user_id"),
                @Index(name = "idx_user_progress_lesson", columnList = "lesson_id")
        }
)
@Check(constraints = "stars_earned BETWEEN 0 AND 3 AND attempts >= 0 AND time_spent IS NULL OR time_spent >= 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "user_progress_user_id_fkey")
    )
    private User user;

    // SQL: lesson_id INTEGER REFERENCES lessons(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lesson_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "user_progress_lesson_id_fkey")
    )
    private Lesson lesson;

    // SQL: completed_at TIMESTAMP
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // SQL: score INTEGER
    @Column(name = "score")
    private Integer score;

    // SQL: attempts INTEGER DEFAULT 0
    @Min(0)
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    // SQL: is_completed BOOLEAN DEFAULT FALSE
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    // SQL: stars_earned INTEGER CHECK (stars_earned BETWEEN 0 AND 3)
    @Min(0) @Max(3)
    @Column(name = "stars_earned")
    private Integer starsEarned;

    // SQL: time_spent INTEGER -- seconds
    @Column(name = "time_spent")
    private Integer timeSpent; // seconds

    /* ---------- convenience helpers ---------- */

    public void incrementAttempts() {
        if (this.attempts == null) this.attempts = 0;
        this.attempts++;
    }

    public void markCompleted(LocalDateTime when, Integer score, Integer stars, Integer timeSpentSec) {
        this.isCompleted = true;
        this.completedAt = when != null ? when : LocalDateTime.now();
        this.score = score;
        this.starsEarned = stars;
        this.timeSpent = timeSpentSec;
    }
}