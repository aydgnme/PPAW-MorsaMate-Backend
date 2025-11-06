package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for refunding a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequest {

    /**
     * Partial refund amount (optional).
     * If not specified, full refund will be issued.
     */
    private BigDecimal amount;

    /**
     * Reason for refund.
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
