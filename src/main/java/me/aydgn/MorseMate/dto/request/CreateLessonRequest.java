package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateLessonRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description; // TEXT

    // "BEGINNER" | "INTERMEDIATE" | "ADVANCED" (matches Lesson.Difficulty enum)
    @NotNull
    private String difficulty;

    private String content;    // TEXT
    private Integer orderIndex;

    @Min(0)
    private Integer pointsReward;     // default 10 if null

    @Min(0)
    private Integer estimatedDuration; // minutes, nullable
}