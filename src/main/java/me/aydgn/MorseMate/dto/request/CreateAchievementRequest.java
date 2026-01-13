package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CreateAchievementRequest {
    @NotBlank
    private String name;
    private String description;
    private String iconUrl;
    private String category;
    private String rarity;

    private String criteriaType;
    @Min(1)
    private Integer criteriaTarget;
    private Map<String, Object> criteriaMetadata;

    @Min(0)
    private Integer points;
    @Min(0)
    private Integer gemReward;
}
