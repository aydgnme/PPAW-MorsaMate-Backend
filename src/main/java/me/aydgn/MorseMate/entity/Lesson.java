package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lesson entity with soft delete support
 * When deleted, only deleted_at timestamp is set
 * Queries automatically filter out deleted records
 */
@Entity
@Table(name = "lessons")
@SQLDelete(sql = "UPDATE lessons SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "lessons_category_id_fkey")
    )
    private Category category;

    // SQL: title VARCHAR(200) NOT NULL
    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // SQL: description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // SQL: difficulty VARCHAR(20) CHECK ('beginner'|'intermediate'|'advanced')
    // App-level enum; DB'de VARCHAR saklanır (CHECK'i şema enforce eder)
    public enum Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private Difficulty difficulty;

    // SQL: content TEXT
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // SQL: order_index INTEGER
    @Column(name = "order_index")
    private Integer orderIndex;

    // SQL: points_reward INTEGER DEFAULT 10
    @Column(name = "points_reward", nullable = false)
    @Builder.Default
    private Integer pointsReward = 10;

    // SQL: estimated_duration INTEGER (minutes)
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    // SQL: created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // SQL: updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Optional: bi-directional to exercises (exercises.lesson_id)
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, orphanRemoval = false)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    /* ---------------- lifecycle hooks ---------------- */

    @PrePersist
    private void prePersist() {
        final LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;

        if (this.title != null) this.title = this.title.trim();
        if (this.description != null) this.description = this.description.trim();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.title != null) this.title = this.title.trim();
        if (this.description != null) this.description = this.description.trim();
    }

    /* -------- convenience helpers for bidirectional sync -------- */

    public void addExercise(Exercise exercise) {
        if (exercise == null) return;
        exercises.add(exercise);
        exercise.setLesson(this);
    }

    public void removeExercise(Exercise exercise) {
        if (exercise == null) return;
        exercises.remove(exercise);
        // ilişkiyi yöneten taraf Exercise (ManyToOne) olduğu için:
        if (exercise.getLesson() == this) {
            exercise.setLesson(null);
        }
    }
}