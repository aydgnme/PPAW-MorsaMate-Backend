package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.CreateAchievementRequest;
import me.aydgn.MorseMate.dto.request.UpdateAchievementRequest;
import me.aydgn.MorseMate.dto.response.AchievementResponse;

import java.util.Map;

/**
 * Form backing bean for admin achievement management views.
 */
@Getter
@Setter
@NoArgsConstructor
public class AchievementForm {

    private Long id;

    @NotBlank(message = "Achievement name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    private String iconUrl;

    private String criteriaJson;

    private String category = "PROGRESS";
    private String rarity = "COMMON";
    private String criteriaType = "LESSONS_COMPLETED";
    private Integer criteriaTarget = 1;

    @Min(value = 0, message = "Points must be non-negative")
    private Integer points = 10;

    @Min(value = 0, message = "Gem reward must be non-negative")
    private Integer gemReward = 0;

    public CreateAchievementRequest toCreateRequest() {
        return CreateAchievementRequest.builder()
                .name(name)
                .description(description)
                .iconUrl(iconUrl)
                .category(category)
                .rarity(rarity)
                .criteriaType(criteriaType)
                .criteriaTarget(criteriaTarget)
                .criteriaMetadata(parseCriteriaJson())
                .points(points != null ? points : 10)
                .gemReward(gemReward != null ? gemReward : 0)
                .build();
    }

    public UpdateAchievementRequest toUpdateRequest() {
        return UpdateAchievementRequest.builder()
                .name(name)
                .description(description)
                .iconUrl(iconUrl)
                .category(category)
                .rarity(rarity)
                .criteriaType(criteriaType)
                .criteriaTarget(criteriaTarget)
                .criteriaMetadata(parseCriteriaJson())
                .points(points)
                .gemReward(gemReward)
                .build();
    }

    public static AchievementForm from(AchievementResponse achievement) {
        AchievementForm form = new AchievementForm();
        form.setId(achievement.getId());
        form.setName(achievement.getName());
        form.setDescription(achievement.getDescription());
        form.setIconUrl(achievement.getIconUrl());
        form.setCategory(achievement.getCategory());
        form.setRarity(achievement.getRarity());
        form.setCriteriaType(achievement.getCriteriaType());
        form.setCriteriaTarget(achievement.getCriteriaTarget());
        form.setCriteriaJson(
                achievement.getCriteriaMetadata() != null ? achievement.getCriteriaMetadata().toString() : null);
        form.setPoints(achievement.getPoints());
        form.setGemReward(achievement.getGemReward());
        return form;
    }

    private Map<String, Object> parseCriteriaJson() {
        if (criteriaJson == null || criteriaJson.isBlank()) {
            return null;
        }
        try {
            return java.util.Collections.emptyMap();
        } catch (Exception e) {
            return null;
        }
    }
}
