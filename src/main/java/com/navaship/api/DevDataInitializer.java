package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.navaship.api.address.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.easypost.EasypostService;
import com.navaship.api.packages.PackageService;
import com.navaship.api.security.PasswordEncoder;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscription.SubscriptionPlanService;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.navaship.api.util.PasswordGenerator;
import com.navaship.api.webhook.Webhook;
import com.navaship.api.webhook.WebhookService;
import com.navaship.api.webhook.WebhookType;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.WebhookEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dummy data generated for dev env only
 */
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionDetailService subscriptionDetailService;
    private final AppUserService appUserService;
    private final AddressService addressService;
    private final PackageService packageService;
    private final StripeService stripeService;
    private final EasypostService easyPostService;
    private final WebhookService webhookService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final List<SubscriptionPlan> defaultSubscriptionPlans;

    @Value("${easypost.webhook.endpoint.url}")
    private String easypostWebhookUrl;
    @Value("${stripe.webhook.endpoint.url}")
    private String stripeWebhookUrl;


    @Override
    public void run(String... args) {
        AppUser admin = new AppUser("admin@lol.com", passwordEncoder.bCryptPasswordEncoder().encode("admin123"), "admin", "admin",
                "5146662222", "New york", "NY", "899 road", AppUserRoleEnum.ADMIN);
        admin.setIsEmailVerified(true);

        try {
            Customer customer = stripeService.createCustomer(admin);
            SubscriptionDetail subscriptionDetail = new SubscriptionDetail();
            subscriptionDetail.setStripeCustomerId(customer.getId());
            subscriptionDetail.setUser(admin);
            subscriptionDetailService.createSubscriptionDetail(subscriptionDetail);
        } catch (StripeException e) {
            System.out.println("Error creating Stripe customer for default admin account " + e.getMessage());
        }

        addressService.createAddress("417 Montgomery St Ste 500", "San Francisco", "CA", "94104", "US", true, admin);
        addressService.createAddress("181 Fremont St", "San Francisco", "CA", "94104", "US", true, admin);
        packageService.createPackage("Letter", BigDecimal.valueOf(12), BigDecimal.valueOf(12), BigDecimal.valueOf(12), BigDecimal.valueOf(10), admin);

        defaultSubscriptionPlans.add(new SubscriptionPlan("Basic", "Create up to 200 shipments a month", "", new BigDecimal("0.02"), 200));
        defaultSubscriptionPlans.add(new SubscriptionPlan("Standard", "Create up to 300 shipments a month", "", new BigDecimal("0.015"), 300));
        defaultSubscriptionPlans.add(new SubscriptionPlan("Premium", "Create up to 500 shipments a month", "", new BigDecimal("0.01"), 500));

        try {
            List<Price> prices = stripeService.retrievePrices();

            for (int i = 0; i < prices.size(); i++) {
                SubscriptionPlan defaultSubscriptionPlan = defaultSubscriptionPlans.get(i);
                defaultSubscriptionPlan.setStripePriceId(prices.get(i).getId());
                subscriptionPlanService.createSubscriptionPlan(defaultSubscriptionPlan);
            }
        } catch (StripeException e) {
            System.out.println("Error retrieving prices " + e.getMessage());
        }

        // Create easypost webhook for the guy so that the doesn't create it manually
        if (webhookService.retrieveWebhookWithType(WebhookType.EASYPOST) == null) {
            try {
                String easypostWebhookSecret = passwordGenerator.generateStrongKey();
                com.easypost.model.Webhook easypostWebhook = easyPostService.createWebhook(easypostWebhookUrl, easypostWebhookSecret, "test");
                Webhook webhook = new Webhook();
                webhook.setType(WebhookType.EASYPOST);
                webhook.setWebhookId(easypostWebhook.getId());
                webhook.setUrl(easypostWebhookUrl);
                webhook.setSecret(easypostWebhookSecret);
                webhookService.createWebhook(webhook);
                System.out.println("> Created Easypost webhook " + webhook.getUrl());
            } catch (EasyPostException e) {
                System.out.println("> Error creating Easypost webhook. You have to do it manually if the script is unable to create it");
                System.out.println("> Reason: " + e.getMessage());
            }
        }

        if (webhookService.retrieveWebhookWithType(WebhookType.STRIPE) == null) {
            try {
                WebhookEndpoint stripeWebhook = stripeService.createWebhook(stripeWebhookUrl);
                Webhook webhook = new Webhook();
                webhook.setType(WebhookType.STRIPE);
                webhook.setWebhookId(stripeWebhook.getId());
                webhook.setUrl(stripeWebhookUrl);
                webhook.setSecret(stripeWebhook.getSecret());
                webhookService.createWebhook(webhook);
                System.out.println("> Created Stripe webhook " + webhook.getUrl());
            } catch (StripeException e) {
                System.out.println("> Error creating Stripe webhook. You have to do it manually if the script is unable to create it");
                System.out.println("> Reason: " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void cleanUpService() {
        Webhook easypostWebhook = webhookService.retrieveWebhookWithType(WebhookType.EASYPOST);
        try {
            easyPostService.deleteWebhook(easypostWebhook.getWebhookId());
            System.out.println("> Deleted Easypost webhook " + easypostWebhook.getWebhookId());
        } catch (EasyPostException e) {
            System.out.println("> Error deleting Easypost webhook " + e.getMessage());
        }

        Webhook stripeWebhook = webhookService.retrieveWebhookWithType(WebhookType.STRIPE);
        try {
            stripeService.deleteWebhook(stripeWebhook.getWebhookId());
            System.out.println("> Deleted Stripe webhook " + stripeWebhook.getWebhookId());
        } catch (StripeException e) {
            System.out.println("> Error deleting Stripe webhook " + e.getMessage());
        }
    }

}