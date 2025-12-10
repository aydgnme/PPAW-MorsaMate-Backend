package me.aydgn.MorseMate.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponseDto {
    private String clientSecret;
    private Long paymentId;
}
