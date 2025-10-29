package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePowerUpRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Cost in gems is required")
    @Min(value = 0, message = "Cost in gems must be at least 0")
    private Integer costGems;

    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    private Boolean isActive;
}
