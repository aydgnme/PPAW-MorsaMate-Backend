package me.aydgn.MorseMate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for Payment entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long subscriptionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String stripePaymentId;
    private LocalDateTime transactionDate;
    private Map<String, Object> metadata;

    /**
     * Indicates if this is a simulated payment (not real Stripe).
     */
    @Builder.Default
    private Boolean simulated = true;
}
