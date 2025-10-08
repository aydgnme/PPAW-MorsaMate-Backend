package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateLessonRequest {

    @NotNull
    private Long id;           // which lesson

    private Long categoryId;   // optional change
    @Size(max = 200)
    private String title;

    private String description;
    private String difficulty;     // enum name
    private String content;
    private Integer orderIndex;

    @Min(0)
    private Integer pointsReward;

    @Min(0)
    private Integer estimatedDuration;
}