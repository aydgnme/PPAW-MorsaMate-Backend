package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.CreatePowerUpRequest;
import me.aydgn.MorseMate.dto.request.UpdatePowerUpRequest;
import me.aydgn.MorseMate.dto.response.PowerUpResponse;
import me.aydgn.MorseMate.entity.PowerUp;

/**
 * Form backing bean for admin power-up management views.
 */
@Getter
@Setter
@NoArgsConstructor
public class PowerUpForm {

    private Long id;

    @NotBlank(message = "Power-up name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Type is required")
    private PowerUp.Type type;

    @NotNull(message = "Cost in gems is required")
    @Min(value = 0, message = "Cost must be non-negative")
    private Integer costGems;

    private Integer durationHours; // Optional, null for instant power-ups

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    private Boolean isActive = true;

    public CreatePowerUpRequest toCreateRequest() {
        return CreatePowerUpRequest.builder()
                .name(name)
                .description(description)
                .type(type != null ? type.name() : null)
                .costGems(costGems)
                .durationHours(durationHours)
                .icon(icon)
                .isActive(isActive != null ? isActive : Boolean.TRUE)
                .build();
    }

    public UpdatePowerUpRequest toUpdateRequest() {
        return UpdatePowerUpRequest.builder()
                .name(name)
                .description(description)
                .type(type != null ? type.name() : null)
                .costGems(costGems)
                .durationHours(durationHours)
                .icon(icon)
                .isActive(isActive)
                .build();
    }

    public static PowerUpForm from(PowerUpResponse powerUp) {
        PowerUpForm form = new PowerUpForm();
        form.setId(powerUp.getId());
        form.setName(powerUp.getName());
        form.setDescription(powerUp.getDescription());
        form.setType(PowerUp.Type.valueOf(powerUp.getType()));
        form.setCostGems(powerUp.getCostGems());
        form.setDurationHours(powerUp.getDurationHours());
        form.setIcon(powerUp.getIcon());
        form.setIsActive(powerUp.getIsActive() != null ? powerUp.getIsActive() : Boolean.TRUE);
        return form;
    }
}
