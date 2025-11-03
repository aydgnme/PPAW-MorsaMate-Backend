package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aydgn.MorseMate.entity.UserSubscription;

import java.time.LocalDateTime;

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
     * Details of the subscription plan.
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
    private LocalDateTime startDate;

    /**
     * When the subscription ends/ended.
     * Null for indefinite subscriptions.
     */
    private LocalDateTime endDate;

    /**
     * Next billing date for auto-renewal.
     */
    private LocalDateTime nextBillingDate;

    /**
     * Whether auto-renewal is enabled.
     */
    private Boolean autoRenew;

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
    private LocalDateTime createdAt;

    /**
     * When the subscription was last updated.
     */
    private LocalDateTime updatedAt;

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

        // Check if subscription is currently active
        boolean isActive = subscription.getStatus() == UserSubscription.Status.ACTIVE
                && (subscription.getEndDate() == null || subscription.getEndDate().isAfter(LocalDateTime.now()));

        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                .username(subscription.getUser() != null ? subscription.getUser().getUsername() : null)
                .plan(subscription.getPlan() != null ? SubscriptionPlanResponse.from(subscription.getPlan()) : null)
                .status(subscription.getStatus() != null ? subscription.getStatus().name() : null)
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .autoRenew(subscription.getAutoRenew())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .isActive(isActive)
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    /**
     * Static factory method for lightweight response (without plan details).
     *
     * @param subscription UserSubscription entity
     * @return UserSubscriptionResponse DTO with minimal plan info
     */
    public static UserSubscriptionResponse fromMinimal(UserSubscription subscription) {
        if (subscription == null) {
            return null;
        }

        boolean isActive = subscription.getStatus() == UserSubscription.Status.ACTIVE
                && (subscription.getEndDate() == null || subscription.getEndDate().isAfter(LocalDateTime.now()));

        UserSubscriptionResponse response = UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                .username(subscription.getUser() != null ? subscription.getUser().getUsername() : null)
                .status(subscription.getStatus() != null ? subscription.getStatus().name() : null)
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .autoRenew(subscription.getAutoRenew())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .isActive(isActive)
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();

        // Add minimal plan info
        if (subscription.getPlan() != null) {
            response.setPlan(SubscriptionPlanResponse.builder()
                    .id(subscription.getPlan().getId())
                    .name(subscription.getPlan().getName())
                    .price(subscription.getPlan().getPrice())
                    .build());
        }

        return response;
    }
}
