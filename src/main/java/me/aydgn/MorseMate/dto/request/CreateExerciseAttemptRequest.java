package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateExerciseAttemptRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long exerciseId;

    /** Free-form answer (Morse/Text/Option). Server will evaluate correctness. */
    private String userAnswer;

    /** Seconds spent for this attempt (optional). */
    @Min(0)
    private Integer timeTaken;
}