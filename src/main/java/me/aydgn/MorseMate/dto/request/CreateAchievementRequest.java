package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAchievementRequest {

    @NotBlank(message = "Achievement name is required")
    @Size(max = 100, message = "Achievement name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    private Map<String, Object> criteria;

    @Min(value = 0, message = "Points must be at least 0")
    private Integer points;

    @Min(value = 0, message = "Gem reward must be at least 0")
    private Integer gemReward;
}
