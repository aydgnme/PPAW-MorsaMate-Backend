package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.UserProgress;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProgressResponse {

    private Long id;

    // flattened relations
    private Long userId;
    private Long lessonId;
    private String lessonTitle;

    private LocalDateTime completedAt;
    private Integer score;
    private Integer attempts;
    private Boolean isCompleted;
    private Integer starsEarned;  // 0..3
    private Integer timeSpent;    // seconds

    public static UserProgressResponse from(UserProgress up) {
        if (up == null) return null;
        return UserProgressResponse.builder()
                .id(up.getId())
                .userId(up.getUser() != null ? up.getUser().getId() : null)
                .lessonId(up.getLesson() != null ? up.getLesson().getId() : null)
                .lessonTitle(up.getLesson() != null ? up.getLesson().getTitle() : null)
                .completedAt(up.getCompletedAt())
                .score(up.getScore())
                .attempts(up.getAttempts())
                .isCompleted(up.getIsCompleted())
                .starsEarned(up.getStarsEarned())
                .timeSpent(up.getTimeSpent())
                .build();
    }
}