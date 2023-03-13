package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanRequest {
    @NotNull(message = "Name is required")
    @NotEmpty
    private String name;
    @NotNull(message = "Description is required")
    @NotEmpty
    private String description;
    @NotNull(message = "Stripe Price Id is required")
    @NotEmpty
    private String stripePriceId;
    @NotNull(message = "Shipment handling fee is required")
    @DecimalMax(value = "100.00", message = "Handling fee percentage must be less than or equal to 100")
    @Positive(message = "Handling fee percentage must be greater than 0")
    private BigDecimal handlingFeePercentage;
    @NotNull(message = "Max limit is required")
    @Min(value = 1, message = "Max limit must be greater than or equal to 1")
    @Max(value = 100000, message = "Max limit must be less than or equal to 100000")
    private int maxLimit;
}
