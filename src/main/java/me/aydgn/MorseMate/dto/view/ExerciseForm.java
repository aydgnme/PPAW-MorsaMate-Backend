package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.CreateExerciseRequest;
import me.aydgn.MorseMate.dto.request.UpdateExerciseRequest;
import me.aydgn.MorseMate.dto.response.ExerciseResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Form backing bean for admin exercise management views.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExerciseForm {

    private Long id;

    @NotNull(message = "Lesson is required")
    private Long lessonId;

    @NotBlank(message = "Exercise type is required")
    private String type;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    /**
     * Multi-line text area that maps to multiple-choice options.
     * Only required/used when the exercise type supports options.
     */
    private String optionsRaw;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @Min(value = 0, message = "Points must be zero or greater")
    private Integer points;

    @Min(value = 0, message = "Time limit must be zero or greater")
    private Integer timeLimit;

    private String hint;

    public CreateExerciseRequest toCreateRequest() {
        return CreateExerciseRequest.builder()
                .lessonId(lessonId)
                .type(type)
                .question(question)
                .correctAnswer(correctAnswer)
                .options(parseOptions())
                .difficulty(difficulty)
                .points(points)
                .timeLimit(timeLimit)
                .hint(hint)
                .build();
    }

    public UpdateExerciseRequest toUpdateRequest() {
        return UpdateExerciseRequest.builder()
                .id(id)
                .lessonId(lessonId)
                .type(type)
                .question(question)
                .correctAnswer(correctAnswer)
                .options(parseOptions())
                .difficulty(difficulty)
                .points(points)
                .timeLimit(timeLimit)
                .hint(hint)
                .build();
    }

    private List<String> parseOptions() {
        if (optionsRaw == null) {
            return null;
        }

        List<String> options = Arrays.stream(optionsRaw.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        return options.isEmpty() ? null : options;
    }

    public static ExerciseForm from(ExerciseResponse response) {
        ExerciseForm form = new ExerciseForm();
        form.setId(response.getId());
        form.setLessonId(response.getLessonId());
        form.setType(response.getType());
        form.setQuestion(response.getQuestion());
        form.setCorrectAnswer(response.getCorrectAnswer());
        if (response.getOptions() != null) {
            String raw = response.getOptions().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
            form.setOptionsRaw(raw);
        }
        form.setDifficulty(response.getDifficulty());
        form.setPoints(response.getPoints());
        form.setTimeLimit(response.getTimeLimit());
        form.setHint(response.getHint());
        return form;
    }
}
