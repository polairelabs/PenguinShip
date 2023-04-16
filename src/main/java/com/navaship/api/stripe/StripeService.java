package com.navaship.api.stripe;

import com.navaship.api.appuser.AppUser;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.WebhookEndpointListParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeService {
    @Value("${stripe.apikey}")
    private String stripeApiKey;
    @Value("${stripe.productId}")
    private String stripeProductId;
    @Value("${stripe.maxMembershipsAllowed}")
    private int maxMembershipsAllowed;
    @Value("${stripe.trialPeriodDays}")
    private int trialPeriodDays;
    @Value("${navaship.webapp.url}")
    private String webAppUrl;


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
        params.put("limit", maxMembershipsAllowed);
        params.put("product", stripeProductId);
        return Price.list(params).getData();
    }

    public Price retrievePrice(String priceId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Price.retrieve(priceId);
    }

    public Session createCheckoutSession(String baseUrl, String priceId, String customerId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        // https://stripe.com/docs/payments/checkout/free-trials
        // Free trial settings, by default, for all plans
        SessionCreateParams.SubscriptionData.Builder subscriptionBuilder = SessionCreateParams.SubscriptionData.builder()
                .setTrialPeriodDays((long) trialPeriodDays)
                .setTrialSettings(
                        SessionCreateParams.SubscriptionData.TrialSettings.builder()
                                .setEndBehavior(
                                        SessionCreateParams.SubscriptionData.TrialSettings.EndBehavior.builder()
                                                .setMissingPaymentMethod(SessionCreateParams.SubscriptionData.TrialSettings.EndBehavior.MissingPaymentMethod.CANCEL)
                                                .build()
                                )
                                .build()
                );
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(priceId).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(baseUrl + "?success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "?canceled=true")
                .setCustomer(customerId)
                .setPaymentMethodCollection(SessionCreateParams.PaymentMethodCollection.ALWAYS)
                .setSubscriptionData(subscriptionBuilder.build())
                .build();

        return Session.create(params);
    }

    public com.stripe.model.billingportal.Session createStripePortalUrl(String customerId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("return_url", webAppUrl + "/account-settings");

        return com.stripe.model.billingportal.Session.create(params);
    }

    public PaymentIntent createPaymentIntent(String customerId, int amountInCents, String currency) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Customer customer = Customer.retrieve(customerId);
        String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", currency);
        // Will retrieve the default source payment of the customer (the one used for paying the subscription)
        params.put("payment_method", defaultPaymentMethodId);
        params.put("customer", customerId);

        return PaymentIntent.create(params);
    }

    public WebhookEndpoint findWebhookByUrl(String webhookEndpointUrl) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        WebhookEndpointListParams params = WebhookEndpointListParams.builder()
                .setLimit(100L)
                .build();

        WebhookEndpointCollection webhookEndpoints = WebhookEndpoint.list(params);

        for (WebhookEndpoint webhookEndpoint : webhookEndpoints.getData()) {
            if (webhookEndpoint.getUrl().equals(webhookEndpointUrl)) {
                return webhookEndpoint;
            }
        }
        return null;
    }

    public WebhookEndpoint createWebhook(String webhookEndpointUrl) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        List<Object> enabledEvents = new ArrayList<>();
        enabledEvents.add("invoice.payment_succeeded");
        enabledEvents.add("customer.subscription.deleted");
        enabledEvents.add("customer.subscription.created");
        enabledEvents.add("customer.subscription.updated");
        enabledEvents.add("customer.updated");

        Map<String, Object> params = new HashMap<>();
        params.put("url", webhookEndpointUrl);
        params.put("enabled_events", enabledEvents);

        return WebhookEndpoint.create(params);
    }

    public String getWebhookSecret(String webhookId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        WebhookEndpoint webhookEndpoint = WebhookEndpoint.retrieve(webhookId);
        return webhookEndpoint.getSecret();
    }

    public void deleteWebhook(String webhookId) throws StripeException {
        WebhookEndpoint endpoint = WebhookEndpoint.retrieve(webhookId);
        endpoint.delete();
    }

    public Refund refund(String chargeId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(chargeId)
                .build();

        return Refund.create(params);
    }

    public double getRefundAmount(Refund refund) {
        long refundAmountInCents = refund.getAmount();
        return refundAmountInCents / 100.0;
    }
}
