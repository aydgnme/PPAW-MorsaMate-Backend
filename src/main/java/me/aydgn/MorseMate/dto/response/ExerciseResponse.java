package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.Exercise;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExerciseResponse {

    private Long id;

    private Long lessonId;
    private String lessonTitle;     // convenience

    private String type;            // enum name
    private String question;
    private String correctAnswer;
    private List<String> options;

    private String difficulty;      // enum name
    private Integer points;
    private Integer timeLimit;
    private String hint;

    private LocalDateTime createdAt;

    public static ExerciseResponse from(Exercise e, boolean includeAnswer) {
        if (e == null) return null;
        return ExerciseResponse.builder()
                .id(e.getId())
                .lessonId(e.getLesson() != null ? e.getLesson().getId() : null)
                .lessonTitle(e.getLesson() != null ? e.getLesson().getTitle() : null)
                .type(e.getType() != null ? e.getType().name() : null)
                .question(e.getQuestion())
                .correctAnswer(includeAnswer ? e.getCorrectAnswer() : null)
                .options(e.getOptions())
                .difficulty(e.getDifficulty() != null ? e.getDifficulty().name() : null)
                .points(e.getPoints())
                .timeLimit(e.getTimeLimit())
                .hint(e.getHint())
                .createdAt(e.getCreatedAt())
                .build();
    }
}