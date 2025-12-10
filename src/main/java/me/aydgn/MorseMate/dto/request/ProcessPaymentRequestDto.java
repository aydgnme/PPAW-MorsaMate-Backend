package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessPaymentRequestDto {
    @NotNull
    private Long paymentId;

    @NotBlank
    private String paymentMethodId;
}
