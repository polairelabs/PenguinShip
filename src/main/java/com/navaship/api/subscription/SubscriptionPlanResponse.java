package com.navaship.api.subscription;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stripe.model.Price;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanResponse {
    private String name;
    private String description;
    private String stripePriceId;
    private String currency;
    private Long unitAmount;
}
