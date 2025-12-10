package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewPaymentRequestDto {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethodId;

    private Long subscriptionId;
}
