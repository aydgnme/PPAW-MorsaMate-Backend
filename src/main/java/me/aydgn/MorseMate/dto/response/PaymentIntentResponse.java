package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for Stripe Payment Intent (simulated).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponse {

    /**
     * Payment Intent ID (simulated: pi_sim_xxx).
     */
    private String id;

    /**
     * Client secret for frontend confirmation.
     */
    private String clientSecret;

    /**
     * Amount in smallest currency unit.
     */
    private Long amount;

    /**
     * Amount in decimal format.
     */
    private BigDecimal amountDecimal;

    /**
     * Currency code.
     */
    private String currency;

    /**
     * Payment Intent status.
     * Values: requires_payment_method, requires_confirmation, requires_action,
     *         processing, requires_capture, canceled, succeeded
     */
    private String status;

    /**
     * Payment method types.
     */
    private String[] paymentMethodTypes;

    /**
     * Metadata attached to the intent.
     */
    private Map<String, String> metadata;

    /**
     * Indicates this is a simulated Stripe response.
     */
    @Builder.Default
    private Boolean simulated = true;

    /**
     * Timestamp when created.
     */
    private Long created;
}
