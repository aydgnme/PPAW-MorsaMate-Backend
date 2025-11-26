package me.aydgn.MorseMate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for simulated payment charge.
 */
@Data
@Builder
public class PaymentResponseDTO {

    private String status;          // SUCCESS, FAILED
    private String failureReason;   // if failed

    private BigDecimal amount;
    private String currency;

    private Long planId;
    private String planName;

    private String transactionRef;
    private LocalDateTime processedAt;

    private Long cardId;
    private PaymentCardDTO card;
}


