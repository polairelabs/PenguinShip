package com.navaship.api;

import com.navaship.api.address.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.packages.PackageService;
import com.navaship.api.security.PasswordEncoder;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscription.SubscriptionPlanService;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dummy data generated for dev env only
 */
@Component
@AllArgsConstructor
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {
    private SubscriptionPlanService subscriptionPlanService;
    private SubscriptionDetailService subscriptionDetailService;
    private AppUserService appUserService;
    private AddressService addressService;
    private PackageService packageService;
    private StripeService stripeService;
    private PasswordEncoder passwordEncoder;
    private List<SubscriptionPlan> defaultSubscriptionPlans;


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
    }
}