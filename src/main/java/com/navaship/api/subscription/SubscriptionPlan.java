package com.navaship.api.subscription;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
/**
 * Contains the data related to the subscription/membership. The admin can edit these settings in the webapp
 */
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String stripePriceId;
    // The precision attribute sets the total number of digits stored, and the scale attribute sets the number of digits to the right of the decimal point
    // Maximum value capped to 1.000 (1%)
    @Column(precision = 4, scale = 3)
    private BigDecimal shipmentHandlingFee;
    private int maxLimit;
}
