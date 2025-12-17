package me.aydgn.MorseMate.model.subscription;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the available subscription plans in the application.
 *
 * Each plan is defined here with its name, features, and the corresponding
 * Stripe Price ID. This class acts as a single source of truth for what
 * products are available for purchase.
 *
 * To add a new plan:
 * 1. Create a new Product and Price in your Stripe Dashboard.
 * 2. Copy the Price ID (e.g., "price_1J...").
 * 3. Add a new static final SubscriptionPlan instance here.
 */
@Getter
public class SubscriptionPlan {

    private final String id;
    private final String name;
    private final long price; // Price in cents
    private final String stripePriceId;
    private final List<String> features;

    // --- DEFINE YOUR SUBSCRIPTION PLANS HERE ---

    /**
     * The PRO plan, offering premium features.
     * IMPORTANT: Replace "YOUR_STRIPE_PRICE_ID_HERE" with the actual Price ID from your Stripe dashboard.
     */
    public static final SubscriptionPlan PRO = new SubscriptionPlan(
            "pro",
            "MorseMate Pro",
            999L, // e.g., $9.99
            "price_1P83GXCjBiKRc5zS9Aexn7bF", // Replace with your actual Stripe Price ID for the Pro plan
            Arrays.asList(
                    "Access to all advanced lessons",
                    "Unlimited exercise attempts",
                    "Detailed performance analytics",
                    "Priority support"
            )
    );

    // --- LIST OF ALL AVAILABLE PLANS ---

    /**
     * A list of all plans that are available for purchase.
     */
    public static final List<SubscriptionPlan> ALL_PLANS = Arrays.asList(PRO);


    // --- IMPLEMENTATION DETAILS ---

    private SubscriptionPlan(String id, String name, long price, String stripePriceId, List<String> features) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stripePriceId = stripePriceId;
        this.features = features;
    }

    /**
     * Finds a subscription plan by its local ID (e.g., "pro").
     *
     * @param id The ID of the plan to find.
     * @return The SubscriptionPlan, or null if not found.
     */
    public static SubscriptionPlan findById(String id) {
        return ALL_PLANS.stream()
                .filter(plan -> plan.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
