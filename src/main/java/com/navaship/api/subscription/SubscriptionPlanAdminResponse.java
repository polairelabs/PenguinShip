package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanAdminResponse extends SubscriptionPlanResponse {
    private String stripePriceId;
    private BigDecimal shipmentHandlingFee;
}
