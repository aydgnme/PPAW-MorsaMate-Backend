package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateExerciseRequest {

    @NotNull
    private Long lessonId;

    /** ENUM: ENCODE | DECODE | AUDIO | SPEED_TEST | MULTI_CHOICE */
    @NotBlank
    private String type;

    @NotBlank
    private String question;

    @NotBlank
    private String correctAnswer;

    /** Sadece MULTI_CHOICE için dolu olabilir */
    private List<@NotBlank String> options;

    /** ENUM: EASY | MEDIUM | HARD */
    @NotBlank
    private String difficulty;

    @Min(0)
    private Integer points;      // null -> default 5

    @Min(0)
    private Integer timeLimit;   // seconds, optional

    private String hint;         // optional
}