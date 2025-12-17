package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    /** Optional plan reference used by tests and simulated flows. */
    private Long planId;

    private Long subscriptionId; // Optional: to link payment to a subscription renewal

    /** Flag to simulate failures in tests. */
    @Builder.Default
    private Boolean simulateFailure = false;
}