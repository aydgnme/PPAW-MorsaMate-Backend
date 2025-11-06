package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Stripe Customer (simulated).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeCustomerResponse {

    /**
     * Customer ID (simulated: cus_sim_xxx).
     */
    private String id;

    /**
     * Customer email.
     */
    private String email;

    /**
     * Customer name.
     */
    private String name;

    /**
     * User ID in our system.
     */
    private Long userId;

    /**
     * Indicates this is a simulated Stripe customer.
     */
    @Builder.Default
    private Boolean simulated = true;

    /**
     * Timestamp when created.
     */
    private Long created;
}
