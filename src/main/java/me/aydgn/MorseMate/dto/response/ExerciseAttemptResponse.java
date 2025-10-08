package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.ExerciseAttempt;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExerciseAttemptResponse {

    private Long id;

    private Long userId;
    private Long exerciseId;

    private String userAnswer;
    private Boolean isCorrect;
    private Integer timeTaken;       // seconds
    private Integer pointsEarned;
    private LocalDateTime attemptedAt;

    public static ExerciseAttemptResponse from(ExerciseAttempt ea) {
        if (ea == null) return null;
        return ExerciseAttemptResponse.builder()
                .id(ea.getId())
                .userId(ea.getUser() != null ? ea.getUser().getId() : null)
                .exerciseId(ea.getExercise() != null ? ea.getExercise().getId() : null)
                .userAnswer(ea.getUserAnswer())
                .isCorrect(ea.getIsCorrect())
                .timeTaken(ea.getTimeTaken())
                .pointsEarned(ea.getPointsEarned())
                .attemptedAt(ea.getAttemptedAt())
                .build();
    }
}