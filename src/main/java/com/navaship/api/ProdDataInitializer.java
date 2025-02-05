package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.easypost.EasypostService;
import com.navaship.api.security.PasswordEncoder;
import com.navaship.api.stripe.StripeService;
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
@Profile("prod")
public class ProdDataInitializer implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final AppUserService appUserService;
    private final PasswordGenerator passwordGenerator;
    private final SubscriptionPlanService subscriptionPlanService;
    private final StripeService stripeService;
    private final EasypostService easyPostService;
    private final SubscriptionDetailService subscriptionDetailService;
    private final WebhookService webhookService;

    @Value("${stripe.maxMembershipsAllowed}")
    private int maxMembershipsAllowed;

    @Value("${easypost.webhook.endpoint.url}")
    private String easypostWebhookUrl;
    @Value("${stripe.webhook.endpoint.url}")
    private String stripeWebhookUrl;


    @Override
    public void run(String... args) {
        // If he doesn't have admin account in the db, create one
        String[] adminEmails = new String[2];
        adminEmails[0] = "admin@navaship.io";
        adminEmails[1] = "polairelabs@navaship.io";

        for (String adminEmail : adminEmails) {
            if (appUserService.findByEmail(adminEmail).isEmpty()) {
                String password = passwordGenerator.generateRandomPassword(12);
                AppUser admin = new AppUser(adminEmail, passwordEncoder.bCryptPasswordEncoder().encode(password), "Admin", "Account",
                        "", "New york", "NY", "", AppUserRoleEnum.ADMIN);
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
                System.out.println("> Created admin user " + admin.getEmail() + " with password: " + password);
            }
        }

        if (subscriptionPlanService.retrieveSubscriptionPlans().size() == maxMembershipsAllowed) {
            System.out.println("> Max number of subscriptions allowed already created");
        } else {
            // Create memberships from Stripe product and product prices
            try {
                List<Price> prices = stripeService.retrievePrices();
                for (Price price : prices) {
                    // Can potentially create more than [maxMembershipsAllowed] subscriptions (if we have more than [maxMembershipsAllowed] prices in Stripe product)
                    if (subscriptionPlanService.retrieveSubscriptionPlanByPriceId(price.getId()).isEmpty()) {
                        subscriptionPlanService.createSubscriptionPlan("", "", price.getId(), BigDecimal.valueOf(0.02), 10);
                    }
                }
                // Create empty subscriptions to always have a minimum of [maxMembershipsAllowed] subscriptions (memberships) in the database,
                // if client doesn't have [maxMembershipsAllowed] subscriptions for his product
                for (int i = maxMembershipsAllowed; i > prices.size(); i--) {
                    subscriptionPlanService.createSubscriptionPlan("Place holder #" + i, "", "placeholder_id_" + i, BigDecimal.valueOf(0.02), 10);
                }
            } catch (StripeException e) {
                System.out.println("Error retrieving prices " + e.getMessage());
            }
        }

        if (webhookService.retrieveWebhookWithType(WebhookType.EASYPOST) == null) {
            try {
                String easypostWebhookSecret = passwordGenerator.generateStrongKey();
                com.easypost.model.Webhook newEasypostWebhook = easyPostService.createWebhook(easypostWebhookUrl, easypostWebhookSecret, "production");
                Webhook webhook = new Webhook();
                webhook.setType(WebhookType.EASYPOST);
                webhook.setWebhookId(newEasypostWebhook.getId());
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
                WebhookEndpoint newStripeWebhook = stripeService.createWebhook(stripeWebhookUrl);
                Webhook webhook = new Webhook();
                webhook.setType(WebhookType.STRIPE);
                webhook.setWebhookId(newStripeWebhook.getId());
                webhook.setUrl(stripeWebhookUrl);
                webhook.setSecret(newStripeWebhook.getSecret());
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
            webhookService.deleteWebhook(easypostWebhook);
            System.out.println("> Deleted Easypost webhook " + easypostWebhook.getWebhookId());
        } catch (EasyPostException e) {
            System.out.println("> Error deleting Easypost webhook " + e.getMessage());
        }

        Webhook stripeWebhook = webhookService.retrieveWebhookWithType(WebhookType.STRIPE);
        try {
            stripeService.deleteWebhook(stripeWebhook.getWebhookId());
            webhookService.deleteWebhook(stripeWebhook);
            System.out.println("> Deleted Stripe webhook " + stripeWebhook.getWebhookId());
        } catch (StripeException e) {
            System.out.println("> Error deleting Stripe webhook " + e.getMessage());
        }
    }
}