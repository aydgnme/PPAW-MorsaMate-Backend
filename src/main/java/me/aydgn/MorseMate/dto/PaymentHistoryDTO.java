package me.aydgn.MorseMate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a single subscription-related payment event
 * derived from subscription history.
 */
@Data
@Builder
public class PaymentHistoryDTO {

    private Long subscriptionId;
    private Long planId;
    private String planName;

    private BigDecimal amount;
    private String currency;

    private String type;           // INITIAL, RENEWAL, UPGRADE, DOWNGRADE
    private String status;         // SUCCESS, FAILED

    private LocalDateTime createdAt;
}


