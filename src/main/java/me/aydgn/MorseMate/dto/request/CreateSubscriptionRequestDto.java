package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubscriptionRequestDto {
    @NotBlank
    private String priceId;
}
