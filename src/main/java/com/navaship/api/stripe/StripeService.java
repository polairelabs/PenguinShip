package com.navaship.api.stripe;

import com.navaship.api.appuser.AppUser;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeService {
    @Value("${navaship.app.stripe.apikey}")
    private String stripeApiKey;
    @Value("${navaship.webapp.url}")
    private String domain;

    public Customer createCustomer(AppUser user) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        params.put("name", user.getFirstName() + " " + user.getLastName());

        return Customer.create(params);
    }

    public List<Price> retrievePrices() throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = new HashMap<>();
        return Price.list(params).getData();
    }

    public Price retrievePrice(String priceId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Price.retrieve(priceId);
    }

    public Session createCheckoutSession(String price) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(price).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(domain + "?success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(domain + "?canceled=true")
                .build();

        return Session.create(params);
    }

    public com.stripe.model.billingportal.Session createStripePortalUrl(String customerId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("return_url", domain);

        return com.stripe.model.billingportal.Session.create(params);
    }

    public Subscription subscribe(String customerId, String price) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        List<Object> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("price", price);
        items.add(item1);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", items);

        return Subscription.create(params);
    }
}
