package me.aydgn.MorseMate.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseSummary {
    private Long id;
    private String type;        // Exercise.Type
    private String difficulty;  // Exercise.Difficulty
    private Integer points;
    private Integer timeLimit;  // seconds
}