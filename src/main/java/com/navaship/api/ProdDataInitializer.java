package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.easypost.EasyPostService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
    private final EasyPostService easyPostService;
    private final SubscriptionDetailService subscriptionDetailService;
    private final WebhookService webhookService;

    @Value("${stripe.maxMembershipsAllowed}")
    private int maxMembershipsAllowed;
    @Value("${sendgrid.senderEmail}")
    private String adminEmail;

    @Value("${easypost.webhook.endpoint.url}")
    private String easypostWebhookUrl;
    @Value("${easypost.webhook.endpoint.secret}")
    private String easypostWebhookSecret;


    @Override
    public void run(String... args) {
        // If he doesn't have admin account in the db, create one
        if (appUserService.countAdminUsers() == 0 && appUserService.findByEmail(adminEmail).isEmpty()) {
            String password = passwordGenerator.generateRandomPassword(12);
            AppUser admin = new AppUser("Navaship", "Admin", adminEmail, passwordEncoder.bCryptPasswordEncoder().encode(password),
                    "", "", "", "", AppUserRoleEnum.ADMIN);
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

        if (subscriptionPlanService.retrieveSubscriptionPlans().size() == maxMembershipsAllowed) {
            System.out.println("> Max number of subscriptions allowed already created");
            return;
        }

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

        // Create easypost webhook for the guy so that the doesn't create it manually
        if (webhookService.retrieveWebhookWithType(WebhookType.EASYPOST) == null) {
            try {
                com.easypost.model.Webhook easypostWebhook = easyPostService.createWebhook(easypostWebhookUrl, easypostWebhookSecret, "production");
                Webhook webhook = new Webhook();
                webhook.setType(WebhookType.EASYPOST);
                webhook.setWebhookId(easypostWebhook.getId());
                webhook.setUrl(easypostWebhookUrl);
                webhookService.createWebhook(webhook);
            } catch (EasyPostException e) {
                System.out.println("> Error creating Easypost webhook. You have to do it manually if the script is unable to create it");
                System.out.println("> Reason: " + e.getMessage());
            }
        }
    }
}