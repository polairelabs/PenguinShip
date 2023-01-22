package com.navaship.api.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class will handle retrieving subscriptions/memberships,
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/subscriptions")
public class SubscriptionController {
    private final StripeService stripeService;
    private final AppUserService appUserService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionDetailService subscriptionDetailService;
    @Value("${navaship.app.stripe.webhook.endpoint.secret}")
    private String webhookEndpointSecret;

    @GetMapping("/memberships")
    public ResponseEntity<List<SubscriptionPlanResponse>> retrieveSubscriptions() {
        // Used to retrieve membership details/data + price, will need to create an endpoint to edit these fields for admin
        List<SubscriptionPlanResponse> subscriptionPlanResponses = new ArrayList<>();
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.retrieveSubscriptionPlans();
        try {
            for (SubscriptionPlan subscriptionPlan : subscriptionPlans) {
                SubscriptionPlanResponse subscriptionPlanResponse = subscriptionPlanService.convertToSubscriptionPlanResponse(subscriptionPlan);
                Price price = stripeService.retrievePrice(subscriptionPlan.getStripePriceId());
                subscriptionPlanResponse.setCurrency(price.getCurrency());
                subscriptionPlanResponse.setUnitAmount(price.getUnitAmount());
                subscriptionPlanResponses.add(subscriptionPlanResponse);
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Retrieving memberships error");
        }

        return new ResponseEntity<>(subscriptionPlanResponses, HttpStatus.OK);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam String price, @RequestParam String customerId) {
        // Generate payment and cancel link for subscription (price) for anonymous user
        Session session = null;
        try {
            session = stripeService.createCheckoutSession(price, customerId);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        Map<String, String> stripeUrls = new HashMap<>();
        stripeUrls.put("checkout_url", session.getUrl());
        stripeUrls.put("cancel_url", session.getCancelUrl());

        return new ResponseEntity<>(stripeUrls, HttpStatus.OK);
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<Map<String, String>> provisionSubscriber(@RequestBody String payload, HttpServletRequest request) {
        // Webhook to provision or de-provision customer
        // e.g. once customer pays via /create-checkout-session, it's time to provision the subscription to the customer (set stripe customer id + the stripe subscription id to the current user)
        String customerId = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);
            customerId = jsonNode.path("data").path("object").path("customer").asText();
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }

        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer");
        }

        // SubscriptionDetail contains the user
        SubscriptionDetail subscriptionDetail = subscriptionDetailService.retrieveSubscriptionDetail(customerId);

        Event event = null;
        String sigHeader = request.getHeader("Stripe-Signature");

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookEndpointSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Deserialize Event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        // Handle Event
        String status = "";
        switch (event.getType()) {
            case "invoice.payment_succeeded":
                /*
                    Update the default payment method for the customers

                    When a Subscription is made, your user is invoiced, so it is considered a payment and will appear in the Payments dashboard.
                    You can retrieve the Subscription and access the latest_invoice field to obtain the Invoice object.
                    The Invoice object contains the payment_intent field.
                 */
                status = "Subscription payment success";
                Invoice invoice = (Invoice) stripeObject;
                try {
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(invoice.getPaymentIntent());

                    Map<String, Object> customerParams = new HashMap<>();
                    Map<String, Object> invoiceSettingsParams = new HashMap<>();
                    invoiceSettingsParams.put("default_payment_method", paymentIntent.getPaymentMethod());
                    customerParams.put("invoice_settings", invoiceSettingsParams);
                    Customer.retrieve(invoice.getCustomer()).update(customerParams);
                } catch (StripeException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice failed");
                }
                break;
            case "customer.subscription.deleted":
                status = "Subscription Deleted";
                handleSubscriptionDeleted(subscriptionDetail);
                break;
            case "customer.subscription.created":
            case "customer.subscription.updated":
                status = "Subscription Created/Updated";
                Subscription subscription = (Subscription) stripeObject;
                handleSubscriptionCreated(subscription, subscriptionDetail);
                break;
            default:
                status = "Unhandled event type: " + event.getType();
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", status);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<Map<String, String>> createPortalSession(JwtAuthenticationToken principal) {
        // Creates customer portal session where the customer can manage his subscription
        AppUser user = retrieveUserFromJwt(principal);

        com.stripe.model.billingportal.Session portalSession = null;
        try {
            portalSession = stripeService.createStripePortalUrl(user.getSubscriptionDetail().getStripeCustomerId()); // here
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        Map<String, String> stripeUrls = new HashMap<>();
        stripeUrls.put("portal_url", portalSession.getUrl());

        return new ResponseEntity<>(stripeUrls, HttpStatus.OK);
    }

    @GetMapping("/prices")
    public ResponseEntity<List<Price>> getPrices() {
        try {
            return new ResponseEntity<>(stripeService.retrievePrices(), HttpStatus.OK);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private SubscriptionDetail handleSubscriptionCreated(Subscription subscription, SubscriptionDetail subscriptionDetail) {
        // Update user role to verified
        // TODO: Add more validation to validate the user
        AppUser user = subscriptionDetail.getUser();
        user.setRole(AppUserRole.USER);
        subscriptionDetail.setSubscriptionId(subscription.getId());
        subscriptionDetail.setStartDate(subscription.getStartDate());
        // Will persist user as well as subscriptionDetail
        return subscriptionDetailService.modifySubscriptionDetail(subscriptionDetail);
    }

    private void handleSubscriptionDeleted(SubscriptionDetail subscriptionDetail) {
        AppUser user = subscriptionDetail.getUser();
        user.setRole(AppUserRole.UNPAYED_USER);
        appUserService.modifyUser(user);
        subscriptionDetailService.deleteSubscriptionDetail(user);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }
}
