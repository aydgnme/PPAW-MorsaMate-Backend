package me.aydgn.MorseMate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for exposing non-sensitive payment card information to clients.
 */
@Data
@Builder
public class PaymentCardDTO {

    private Long id;
    private String cardholderName;
    private String cardBrand;
    private String cardLast4;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}


