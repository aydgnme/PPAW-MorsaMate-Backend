package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.PowerUp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PowerUpResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private Integer costGems;
    private Integer durationHours;
    private String icon;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static PowerUpResponse from(PowerUp powerUp) {
        if (powerUp == null) return null;

        return PowerUpResponse.builder()
                .id(powerUp.getId())
                .name(powerUp.getName())
                .description(powerUp.getDescription())
                .type(powerUp.getType() != null ? powerUp.getType().name() : null)
                .costGems(powerUp.getCostGems())
                .durationHours(powerUp.getDurationHours())
                .icon(powerUp.getIcon())
                .isActive(powerUp.getIsActive())
                .createdAt(powerUp.getCreatedAt())
                .build();
    }
}
