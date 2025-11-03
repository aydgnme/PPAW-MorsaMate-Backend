package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.Achievement;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AchievementResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private Map<String, Object> criteria;
    private Integer points;
    private Integer gemReward;

    // For user-specific achievement data
    private Boolean earned;
    private LocalDateTime earnedAt;

    public static AchievementResponse from(Achievement achievement) {
        if (achievement == null) return null;

        return AchievementResponse.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .icon(achievement.getIcon())
                .criteria(achievement.getCriteria())
                .points(achievement.getPoints())
                .gemReward(achievement.getGemReward())
                .earned(false)
                .build();
    }
}
