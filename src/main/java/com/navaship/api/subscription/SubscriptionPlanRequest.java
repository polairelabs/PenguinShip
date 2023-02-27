package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanRequest {
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String[] description;
    @NotNull
    @NotEmpty
    private String stripePriceId;
    // TODO validate digits
    @NotNull
    @NotEmpty
    private BigDecimal shipmentHandlingFee;
    @NotNull
    @NotEmpty
    private int maxLimit;
}
