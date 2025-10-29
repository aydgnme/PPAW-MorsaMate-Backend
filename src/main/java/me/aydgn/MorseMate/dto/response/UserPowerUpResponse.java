package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.UserPowerUp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPowerUpResponse {

    private Long id;
    private Long userId;
    private PowerUpResponse powerUp;
    private LocalDateTime purchasedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Boolean isUsed;

    public static UserPowerUpResponse from(UserPowerUp userPowerUp) {
        if (userPowerUp == null) return null;

        return UserPowerUpResponse.builder()
                .id(userPowerUp.getId())
                .userId(userPowerUp.getUser() != null ? userPowerUp.getUser().getId() : null)
                .powerUp(PowerUpResponse.from(userPowerUp.getPowerUp()))
                .purchasedAt(userPowerUp.getPurchasedAt())
                .activatedAt(userPowerUp.getActivatedAt())
                .expiresAt(userPowerUp.getExpiresAt())
                .isActive(userPowerUp.getIsActive())
                .isUsed(userPowerUp.getIsUsed())
                .build();
    }
}
