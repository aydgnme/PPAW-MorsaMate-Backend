package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity {

    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: lesson_id INTEGER REFERENCES lessons(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lesson_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "exercises_lesson_id_fkey")
    )
    private Lesson lesson;

    // SQL: type VARCHAR(50) CHECK ('encode','decode','audio','speed_test','multi_choice')
    public enum Type { ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private Type type;

    // SQL: question TEXT NOT NULL
    @NotBlank
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    // SQL: correct_answer TEXT NOT NULL
    @NotBlank
    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    // SQL: options JSONB  (for multiple choice)
    // Hibernate 6+: map JSONB using @JdbcTypeCode(SqlTypes.JSON)
    // Here we store as an array of strings; adapt as needed.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    // SQL: difficulty VARCHAR(20) CHECK ('easy','medium','hard')
    public enum Difficulty { EASY, MEDIUM, HARD }

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private Difficulty difficulty;

    // SQL: points INTEGER DEFAULT 5
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 5;

    // SQL: time_limit INTEGER (seconds)
    @Column(name = "time_limit")
    private Integer timeLimit;

    // SQL: hint TEXT
    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;

    // SQL: created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /* ---------------- lifecycle hooks ---------------- */

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.question != null) this.question = this.question.trim();
        if (this.correctAnswer != null) this.correctAnswer = this.correctAnswer.trim();
        if (this.hint != null) this.hint = this.hint.trim();
    }

    /* ------------- convenience for bidirectional link ------------- */

    public void attachTo(Lesson lesson) {
        if (lesson == null) return;
        this.lesson = lesson;
        if (!lesson.getExercises().contains(this)) {
            lesson.getExercises().add(this);
        }
    }
}