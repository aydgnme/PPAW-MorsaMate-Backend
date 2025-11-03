package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aydgn.MorseMate.entity.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String billingPeriod;
    private Map<String, Object> features;
    private Integer maxHearts;
    private Integer dailyPracticeLimit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert SubscriptionPlan entity to response DTO
     */
    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .billingPeriod(plan.getBillingPeriod() != null ? plan.getBillingPeriod().name() : null)
                .features(plan.getFeatures())
                .maxHearts(plan.getMaxHearts())
                .dailyPracticeLimit(plan.getDailyPracticeLimit())
                .isActive(plan.getIsActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
