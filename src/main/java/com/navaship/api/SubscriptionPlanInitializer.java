package com.navaship.api;

import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscription.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanInitializer {
    private final SubscriptionPlanService subscriptionPlanService;


    @PostConstruct
    public void createMembershipsData() {
        SubscriptionPlan subscriptionPlanBasic = new SubscriptionPlan();
        subscriptionPlanBasic.setName("Basic");
        subscriptionPlanBasic.setDescription("Create up to 200 shipments a month");
        subscriptionPlanBasic.setStripePriceId("price_1MGykRBGBO0gISTgtVAocLVV");
        subscriptionPlanBasic.setMaxLimit(200);
        subscriptionPlanBasic.setShipmentHandlingFee(new BigDecimal("0.02"));

        SubscriptionPlan subscriptionPlanStandard = new SubscriptionPlan();
        subscriptionPlanStandard.setName("Standard");
        subscriptionPlanStandard.setDescription("Create up to 300 shipments a month");
        subscriptionPlanStandard.setStripePriceId("price_1MGykRBGBO0gISTgAzsoxDmk");
        subscriptionPlanStandard.setMaxLimit(300);
        subscriptionPlanStandard.setShipmentHandlingFee(new BigDecimal("0.015"));

        SubscriptionPlan subscriptionPlanPremium = new SubscriptionPlan();
        subscriptionPlanPremium.setName("Premium");
        subscriptionPlanPremium.setDescription("Create up to 500 shipments a month");
        subscriptionPlanPremium.setStripePriceId("price_1MGykRBGBO0gISTgNPL2QZLh");
        subscriptionPlanPremium.setMaxLimit(500);
        subscriptionPlanPremium.setShipmentHandlingFee(new BigDecimal("0.01"));

        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanBasic);
        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanStandard);
        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanPremium);
    }
}
