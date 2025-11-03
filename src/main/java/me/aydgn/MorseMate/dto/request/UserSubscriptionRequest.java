package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user subscription operations.
 * Used when subscribing to a new plan or upgrading existing subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionRequest {

    /**
     * ID of the subscription plan to subscribe to.
     * Must reference an existing and active SubscriptionPlan.
     */
    @NotNull(message = "Plan ID is required")
    private Long planId;

    /**
     * Whether to enable auto-renewal for this subscription.
     * Default: true
     */
    @Builder.Default
    private Boolean autoRenew = true;

    /**
     * Optional payment method ID for Stripe integration.
     * Required for paid subscriptions when Stripe is enabled.
     */
    private String paymentMethodId;
}
