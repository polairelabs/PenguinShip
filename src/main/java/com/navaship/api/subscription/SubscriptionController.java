package com.navaship.api.subscription;

import com.navaship.api.appuser.AppUser;
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
                SubscriptionPlanResponse subscriptionPlanResponse = new SubscriptionPlanResponse();
                subscriptionPlanResponse.setName(subscriptionPlan.getName());
                subscriptionPlanResponse.setDescription(subscriptionPlan.getDescription());
                Price price = stripeService.retrievePrice(subscriptionPlan.getStripePriceId());
                subscriptionPlanResponse.setUnitAmount(price.getUnitAmount());
                subscriptionPlanResponse.setCurrency(price.getCurrency());
                subscriptionPlanResponses.add(subscriptionPlanResponse);
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Retrieving memberships error");
        }

        return new ResponseEntity<>(subscriptionPlanResponses, HttpStatus.OK);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam String price) {
        // Generate payment and cancel link for subscription (price) for anonymous user
        Session session = null;
        try {
            session = stripeService.createCheckoutSession(price);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        Map<String, String> stripeUrls = new HashMap<>();
        stripeUrls.put("checkout_url", session.getUrl());
        stripeUrls.put("cancel_url", session.getCancelUrl());

        return new ResponseEntity<>(stripeUrls, HttpStatus.OK);
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<Map<String, String>> provisionSubscriber(JwtAuthenticationToken principal, @RequestBody String payload, HttpServletRequest request) {
        // Webhook to provision or de-provision customer
        // e.g. once customer pays via /create-checkout-session, it's time to provision the subscription to the customer (set stripe customer id + the stripe subscription id to the current user)
        AppUser user = retrieveUserFromJwt(principal);

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
        Subscription subscription = null;
        String status;
        switch (event.getType()) {
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(user);
                status = "Deleted";
                break;
            case "customer.subscription.created":
            case "customer.subscription.updated":
                subscription = (Subscription) stripeObject;
                handleSubscriptionCreated(subscription, user);
                status = "Created";
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
                status = "Unhandled event type: " + event.getType();
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", status);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(JwtAuthenticationToken principal) {
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

    private SubscriptionDetail handleSubscriptionCreated(Subscription subscription, AppUser user) {
        SubscriptionDetail subscriptionDetail = new SubscriptionDetail();
        subscriptionDetail.setUser(user);
        subscriptionDetail.setSubscriptionId(subscription.getId());
        subscriptionDetail.setStripeCustomerId(subscription.getCustomer());
        subscriptionDetail.setStartDate(subscription.getStartDate());
        return subscriptionDetailService.createSubscriptionDetail(subscriptionDetail);
    }

    private void handleSubscriptionDeleted(AppUser user) {
        subscriptionDetailService.deleteSubscriptionDetail(user);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }
}
