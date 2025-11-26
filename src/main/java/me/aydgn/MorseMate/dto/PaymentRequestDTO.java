package me.aydgn.MorseMate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for card-based simulated payments.
 */
@Data
@Builder
public class PaymentRequestDTO {

    private Long planId;

    private BigDecimal amount;

    private String currency;

    /**
     * Existing saved card to use for payment.
     * If null, a new card will be created from raw card data.
     */
    private Long cardId;

    // Raw card data (only used when cardId is null)
    private String cardholderName;
    private String cardNumber;
    private String cvv;
    private Integer expiryMonth;
    private Integer expiryYear;

    /**
     * Whether to save the card for future use.
     */
    @Builder.Default
    private boolean saveCard = true;
}


