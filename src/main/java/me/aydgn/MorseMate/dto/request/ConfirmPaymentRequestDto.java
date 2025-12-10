package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmPaymentRequestDto {
    @NotNull
    private Long paymentId;
}
