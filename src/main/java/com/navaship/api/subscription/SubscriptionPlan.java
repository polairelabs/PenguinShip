package com.navaship.api.subscription;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.auth.AuthViews;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

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
    @GeneratedValue(generator = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;
    @JsonView(AuthViews.Default.class)
    private String name;
    private String description;
    private String stripePriceId;
    // The precision attribute sets the total number of digits stored, and the scale attribute sets the number of digits to the right of the decimal point
    // Maximum value capped to 1.000 (1%)
    @Column(precision = 4, scale = 3)
    private BigDecimal shipmentHandlingFee;
    @JsonView(AuthViews.Default.class)
    private int maxLimit;


    public SubscriptionPlan(String name, String description, String stripePriceId, BigDecimal shipmentHandlingFee, int maxLimit) {
        this.name = name;
        this.description = description;
        this.stripePriceId = stripePriceId;
        this.shipmentHandlingFee = shipmentHandlingFee;
        this.maxLimit = maxLimit;
    }
}
