package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanUpdateResponse {
    private UUID id;
    private String name;
    private String description;
    private String stripePriceId;
    private BigDecimal shipmentHandlingFee;
    private int maxLimit;
}
