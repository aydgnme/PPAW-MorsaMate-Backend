package me.aydgn.MorseMate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Aggregated payment statistics for a user.
 */
@Data
@Builder
public class PaymentStatsDTO {

    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;

    private BigDecimal totalAmount;
    private BigDecimal averageAmount;

    private long activeSubscriptions;
}


