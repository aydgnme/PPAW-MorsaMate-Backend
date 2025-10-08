package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.entity.Lesson;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LessonResponse {

    private Long id;

    // category info (flattened for API)
    private Long categoryId;
    private String categoryName;

    private String title;
    private String description;
    private String difficulty;       // enum name as String
    private String content;

    private Integer orderIndex;
    private Integer pointsReward;
    private Integer estimatedDuration;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // optional embedded exercises (lightweight)
    private List<ExerciseSummary> exercises;

    /* ---------- mapping helpers ---------- */
    public static LessonResponse from(Lesson l, boolean includeExercises) {
        if (l == null) return null;

        LessonResponse resp = LessonResponse.builder()
                .id(l.getId())
                .categoryId(l.getCategory() != null ? l.getCategory().getId() : null)
                .categoryName(l.getCategory() != null ? l.getCategory().getName() : null)
                .title(l.getTitle())
                .description(l.getDescription())
                .difficulty(l.getDifficulty() != null ? l.getDifficulty().name() : null)
                .content(l.getContent())
                .orderIndex(l.getOrderIndex())
                .pointsReward(l.getPointsReward())
                .estimatedDuration(l.getEstimatedDuration())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();

        if (includeExercises && l.getExercises() != null) {
            resp.setExercises(l.getExercises().stream()
                    .map(LessonResponse::toSummary)
                    .collect(Collectors.toList()));
        }
        return resp;
    }

    private static ExerciseSummary toSummary(Exercise e) {
        return ExerciseSummary.builder()
                .id(e.getId())
                .type(e.getType() != null ? e.getType().name() : null)
                .difficulty(e.getDifficulty() != null ? e.getDifficulty().name() : null)
                .points(e.getPoints())
                .timeLimit(e.getTimeLimit())
                .build();
    }
}