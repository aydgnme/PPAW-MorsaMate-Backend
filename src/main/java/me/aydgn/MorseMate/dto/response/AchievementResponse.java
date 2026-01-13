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
    private String iconUrl;
    private String category;
    private String rarity;

    private String criteriaType;
    private Integer criteriaTarget;
    private Map<String, Object> criteriaMetadata;

    private Integer points;
    private Integer gemReward;

    // For user-specific achievement data
    private Boolean earned;
    private LocalDateTime unlockedAt;
    private Integer currentProgress;

    public static AchievementResponse from(Achievement achievement) {
        if (achievement == null)
            return null;

        return AchievementResponse.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .category(achievement.getCategory() != null ? achievement.getCategory().name() : null)
                .rarity(achievement.getRarity() != null ? achievement.getRarity().name() : null)
                .criteriaType(achievement.getCriteriaType() != null ? achievement.getCriteriaType().name() : null)
                .criteriaTarget(achievement.getCriteriaTarget())
                .criteriaMetadata(achievement.getCriteriaMetadata())
                .points(achievement.getPoints())
                .gemReward(achievement.getGemReward())
                .earned(false)
                .build();
    }
}
