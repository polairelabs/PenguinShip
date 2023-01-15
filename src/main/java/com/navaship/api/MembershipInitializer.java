package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Webhook;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscription.SubscriptionPlanService;
import com.navaship.api.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class MembershipInitializer {
    private final SubscriptionPlanService subscriptionPlanService;


    @PostConstruct
    public void createMembershipsData() {
        // TODO: Add the correct details here
        SubscriptionPlan subscriptionPlanBasic = new SubscriptionPlan();
        subscriptionPlanBasic.setName("Basic");
        subscriptionPlanBasic.setDescription("Up to 200 packages a month");
        subscriptionPlanBasic.setStripePriceId("price_1MQSUQDra7bwCGnFTvefPBTj");

        SubscriptionPlan subscriptionPlanStandard = new SubscriptionPlan();
        subscriptionPlanStandard.setName("Standard");
        subscriptionPlanStandard.setDescription("Up to 300 packages a month");
        subscriptionPlanStandard.setStripePriceId("price_1MQbykDra7bwCGnF9qBAiMo2");

        SubscriptionPlan subscriptionPlanPremium = new SubscriptionPlan();
        subscriptionPlanPremium.setName("Premium");
        subscriptionPlanPremium.setDescription("Up to 500 packages a month");
        subscriptionPlanPremium.setStripePriceId("price_1MQbzWDra7bwCGnFdXnoiJv3");

        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanBasic);
        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanStandard);
        subscriptionPlanService.createSubscriptionPlan(subscriptionPlanPremium);
    }
}
