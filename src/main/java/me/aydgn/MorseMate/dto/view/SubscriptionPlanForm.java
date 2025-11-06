package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.SubscriptionPlanRequest;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.entity.SubscriptionPlan;

import java.math.BigDecimal;

/**
 * Form backing bean for admin subscription plan management views.
 */
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPlanForm {

    private Long id;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 50, message = "Plan name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Billing period is required")
    private SubscriptionPlan.BillingPeriod billingPeriod;

    private String featuresJson; // JSON string representation

    @NotNull(message = "Max hearts is required")
    @Min(value = 1, message = "Max hearts must be at least 1")
    @Max(value = 100, message = "Max hearts cannot exceed 100")
    private Integer maxHearts = 5;

    @Min(value = 1, message = "Daily practice limit must be at least 1")
    @Max(value = 1000, message = "Daily practice limit cannot exceed 1000")
    private Integer dailyPracticeLimit;

    private Boolean isActive = true;

    public SubscriptionPlanRequest toRequest() {
        return SubscriptionPlanRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .billingPeriod(billingPeriod)
                .features(null) // TODO: Parse JSON
                .maxHearts(maxHearts)
                .dailyPracticeLimit(dailyPracticeLimit)
                .isActive(isActive != null ? isActive : Boolean.TRUE)
                .build();
    }

    public static SubscriptionPlanForm from(SubscriptionPlanResponse plan) {
        SubscriptionPlanForm form = new SubscriptionPlanForm();
        form.setId(plan.getId());
        form.setName(plan.getName());
        form.setDescription(plan.getDescription());
        form.setPrice(plan.getPrice());
        form.setBillingPeriod(plan.getBillingPeriod() != null ?
                SubscriptionPlan.BillingPeriod.valueOf(plan.getBillingPeriod()) : null);
        form.setFeaturesJson(plan.getFeatures() != null ? plan.getFeatures().toString() : null);
        form.setMaxHearts(plan.getMaxHearts());
        form.setDailyPracticeLimit(plan.getDailyPracticeLimit());
        form.setIsActive(plan.getIsActive() != null ? plan.getIsActive() : Boolean.TRUE);
        return form;
    }
}
