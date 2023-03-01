package com.navaship.api.subscriptiondetail;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.auth.AuthViews;
import com.navaship.api.subscription.SubscriptionPlan;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor
@Entity
/**
 * Contains the data related to the subscription/membership the user subscribed to.
 */
public class SubscriptionDetail {
    @Id
    @Column(name = "user_id")
    @Type(type = "uuid-char")
    private UUID id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;
    private String stripeCustomerId;
    private String subscriptionId;
    @JsonView(AuthViews.Default.class)
    private int currentLimit;
    @OneToOne
    @JsonView(AuthViews.Default.class)
    private SubscriptionPlan subscriptionPlan;
    private Long startDate;
    private Long lastPaymentDate;
    private Long endDate;
    @JsonView(AuthViews.Default.class)
    private String cardType;
    @JsonView(AuthViews.Default.class)
    private String cardLastFourDigits;
    @JsonView(AuthViews.Default.class)
    private String status;
}
