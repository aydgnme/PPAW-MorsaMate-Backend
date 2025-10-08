package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateExerciseRequest {

    @NotNull
    private Long id;

    private Long lessonId;       // optional move

    /** ENUM: ENCODE | DECODE | AUDIO | SPEED_TEST | MULTI_CHOICE */
    private String type;

    private String question;
    private String correctAnswer;

    private List<@NotBlank String> options;

    /** ENUM: EASY | MEDIUM | HARD */
    private String difficulty;

    @Min(0)
    private Integer points;

    @Min(0)
    private Integer timeLimit;

    private String hint;
}