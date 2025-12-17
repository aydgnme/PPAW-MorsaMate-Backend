package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;

import java.time.OffsetDateTime;

/**
 * Response DTO for user subscription information.
 * Contains complete subscription details including plan information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionResponse {

    /**
     * Unique identifier for the subscription.
     */
    private Long id;

    /**
     * ID of the user who owns this subscription.
     */
    private Long userId;

    /**
     * Username of the subscriber.
     */
    private String username;

    /**
     * ID of the subscription plan.
     */
    private String planId;

    /**
     * Full plan details (optional, used in tests/builders).
     */
    private SubscriptionPlanResponse plan;

    /**
     * Current status of the subscription.
     * Values: ACTIVE, CANCELLED, EXPIRED, PAUSED
     */
    private String status;

    /**
     * When the subscription started.
     */
    private OffsetDateTime startDate;

    /**
     * When the subscription ends/ended.
     * Null for indefinite subscriptions.
     */
    private OffsetDateTime endDate;

    /**
     * Stripe subscription ID (if integrated).
     */
    private String stripeSubscriptionId;

    /**
     * Whether the subscription is currently active and valid.
     * True if status is ACTIVE and not expired.
     */
    private Boolean isActive;

    /**
     * When the subscription was created.
     */
    private OffsetDateTime createdAt;

    /**
     * When the subscription was last updated.
     */
    private OffsetDateTime updatedAt;

    /**
     * Static factory method to create response from entity.
     *
     * @param subscription UserSubscription entity
     * @return UserSubscriptionResponse DTO
     */
    public static UserSubscriptionResponse from(UserSubscription subscription) {
        if (subscription == null) {
            return null;
        }

        boolean isActive = subscription.getStatus() == UserSubscription.SubscriptionStatus.ACTIVE
                && (subscription.getCurrentPeriodEnd() == null || subscription.getCurrentPeriodEnd().isAfter(OffsetDateTime.now()));

        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                .username(subscription.getUser() != null ? subscription.getUser().getUsername() : null)
                .planId(subscription.getPlanId().toString())
                .plan(null)
                .status(subscription.getStatus() != null ? subscription.getStatus().name() : null)
                .startDate(subscription.getCurrentPeriodStart())
                .endDate(subscription.getCurrentPeriodEnd())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .isActive(isActive)
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
