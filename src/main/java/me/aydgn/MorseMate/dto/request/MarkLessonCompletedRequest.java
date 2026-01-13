package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkLessonCompletedRequest {

    private Long userId;

    private Long lessonId;

    private LocalDateTime completedAt; // optional; server can default to now()

    private Integer score; // optional
    @Min(0)
    @Max(3)
    private Integer starsEarned; // 0..3, optional
    @Min(0)
    private Integer timeSpent; // seconds, optional
}