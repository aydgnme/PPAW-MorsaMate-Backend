package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating a new payment.
 * Simulates payment creation without actual Stripe integration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    /**
     * Subscription ID this payment is for (optional).
     */
    private Long subscriptionId;

    /**
     * Payment amount.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Currency code (e.g., USD, EUR, TRY).
     */
    @Size(max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment method (e.g., credit_card, paypal, bank_transfer).
     * In simulation mode, any value is accepted.
     */
    @NotNull(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    /**
     * Additional metadata for the payment.
     */
    private Map<String, Object> metadata;

    /**
     * Simulate payment failure for testing (optional).
     * If true, payment will fail with error message.
     */
    @Builder.Default
    private Boolean simulateFailure = false;
}
