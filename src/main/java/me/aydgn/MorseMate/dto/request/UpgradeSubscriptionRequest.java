package me.aydgn.MorseMate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for upgrading a subscription when sending JSON body.
 * Accepts the target plan ID as `newPlanId`.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeSubscriptionRequest {
    private Long newPlanId;
}
