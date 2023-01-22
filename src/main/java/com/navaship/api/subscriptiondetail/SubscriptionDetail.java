package com.navaship.api.subscriptiondetail;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.auth.AuthViews;
import com.navaship.api.subscription.SubscriptionPlan;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class SubscriptionDetail {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    @JsonView(AuthViews.Default.class)
    private String stripeCustomerId;
    private String subscriptionId;

    @OneToOne
    private SubscriptionPlan subscriptionPlan;
    private Long startDate;
}
