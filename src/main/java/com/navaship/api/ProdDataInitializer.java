package com.navaship.api;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.security.PasswordEncoder;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.SubscriptionPlanService;
import com.navaship.api.util.PasswordGenerator;
import com.stripe.exception.StripeException;
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
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final SubscriptionPlanService subscriptionPlanService;
    private final StripeService stripeService;

    @Value("${stripe.maxMembershipsAllowed}")
    private int maxMembershipsAllowed;
    @Value("${sendgrid.senderEmail}")
    private String adminEmail;


    @Override
    public void run(String... args) {
        // If he doesn't have admin account in the db, create one
        if (appUserService.countAdminUsers() == 0 && appUserService.findByEmail(adminEmail).isEmpty()) {
            String password = passwordGenerator.generateRandomPassword(12);
            AppUser admin = new AppUser("Navaship", "Admin", adminEmail, passwordEncoder.bCryptPasswordEncoder().encode(password),
                    "", "", "", "", AppUserRoleEnum.ADMIN);
            appUserService.enableUserAccount(admin);
            System.out.println("Created admin user " + admin.getEmail() + " with password" + password);
        }

        if (subscriptionPlanService.retrieveSubscriptionPlans().size() == maxMembershipsAllowed) {
            System.out.println("Max number of subscriptions allowed already created");
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
    }
}