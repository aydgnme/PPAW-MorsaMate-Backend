package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exercise_attempts",
        indexes = {
                @Index(name = "idx_exercise_attempts_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {

    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "exercise_attempts_user_id_fkey")
    )
    private User user;

    // SQL: exercise_id INTEGER REFERENCES exercises(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exercise_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "exercise_attempts_exercise_id_fkey")
    )
    private Exercise exercise;

    // SQL: user_answer TEXT
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    // SQL: is_correct BOOLEAN
    @Column(name = "is_correct")
    private Boolean isCorrect;

    // SQL: time_taken INTEGER -- seconds
    @Min(0)
    @Column(name = "time_taken")
    private Integer timeTaken;

    // SQL: attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    // SQL: points_earned INTEGER DEFAULT 0
    @Column(name = "points_earned", nullable = false)
    @Builder.Default
    private Integer pointsEarned = 0;

    /* ---------- lifecycle ---------- */
    @PrePersist
    private void prePersist() {
        if (this.attemptedAt == null) {
            this.attemptedAt = LocalDateTime.now();
        }
    }

    /* ---------- helpers ---------- */
    public void markResult(boolean correct, int points, Integer timeSec) {
        this.isCorrect = correct;
        this.pointsEarned = Math.max(0, points);
        this.timeTaken = timeSec;
        if (this.attemptedAt == null) this.attemptedAt = LocalDateTime.now();
    }
}