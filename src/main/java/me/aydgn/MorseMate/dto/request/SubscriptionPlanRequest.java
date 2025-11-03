package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aydgn.MorseMate.entity.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 50, message = "Plan name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Billing period is required")
    private SubscriptionPlan.BillingPeriod billingPeriod;

    private Map<String, Object> features;

    @NotNull(message = "Max hearts is required")
    @Min(value = 1, message = "Max hearts must be at least 1")
    @Max(value = 100, message = "Max hearts cannot exceed 100")
    private Integer maxHearts;

    @Min(value = 1, message = "Daily practice limit must be at least 1")
    @Max(value = 1000, message = "Daily practice limit cannot exceed 1000")
    private Integer dailyPracticeLimit;

    @Builder.Default
    private Boolean isActive = true;
}
