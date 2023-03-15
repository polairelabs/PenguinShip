package com.navaship.api.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.jwt.JwtService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
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
import java.time.Instant;
import java.util.*;

/**
 * The class will handle retrieving subscriptions/memberships,
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/subscriptions")
public class SubscriptionController {
    private final StripeService stripeService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionDetailService subscriptionDetailService;
    private final JwtService jwtService;
    private final AppUserService appUserService;
    @Value("${navaship.app.stripe.webhook.endpoint.secret}")
    private String webhookEndpointSecret;


    @GetMapping()
    public ResponseEntity<List<SubscriptionPlanResponse>> retrieveSubscriptions() {
        // Used to retrieve membership details/data + price
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.retrieveSubscriptionPlans();
        List<SubscriptionPlanResponse> subscriptionPlanResponses = new ArrayList<>();
        List<SubscriptionPlanResponse> sortedSubscriptionPlans = new ArrayList<>();
        try {
            for (SubscriptionPlan subscriptionPlan : subscriptionPlans) {
                SubscriptionPlanResponse subscriptionPlanResponse = subscriptionPlanService.convertToSubscriptionPlanResponse(subscriptionPlan);
                Price price = stripeService.retrievePrice(subscriptionPlan.getStripePriceId());
                subscriptionPlanResponse.setCurrency(price.getCurrency());
                subscriptionPlanResponse.setUnitAmount(price.getUnitAmount());
                subscriptionPlanResponses.add(subscriptionPlanResponse);
                sortedSubscriptionPlans = subscriptionPlanResponses.stream()
                        .sorted(Comparator.comparingLong(SubscriptionPlanResponse::getUnitAmount)).toList();
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Retrieving memberships error");
        }

        return new ResponseEntity<>(sortedSubscriptionPlans, HttpStatus.OK);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam String subscriptionId, @RequestParam String userId) {
        // Generate payment and cancel link for subscription
        SubscriptionPlan subscriptionPlan = subscriptionPlanService.retrieveSubscriptionPlan(subscriptionId);
        AppUser user = appUserService.findById(UUID.fromString(userId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        Session session = null;
        try {
            session = stripeService.createCheckoutSession(subscriptionPlan.getStripePriceId(), user.getSubscriptionDetail().getStripeCustomerId());
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
            case "invoice.payment_succeeded" -> {
                status = "Subscription payment success";
                /*
                    Event occurs whenever an invoice payment attempt succeeds.
                    Update the default payment method for the customers.
                    When a Subscription is made, your user is invoiced, so it is considered a payment and will appear in the Payments dashboard.
                    You can retrieve the Subscription and access the latest_invoice field to obtain the Invoice object.
                    The Invoice object contains the payment_intent field.
                 */
                Invoice invoice = (Invoice) stripeObject;
                try {
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(invoice.getPaymentIntent());

                    Map<String, Object> customerParams = new HashMap<>();
                    Map<String, Object> invoiceSettingsParams = new HashMap<>();
                    invoiceSettingsParams.put("default_payment_method", paymentIntent.getPaymentMethod());
                    customerParams.put("invoice_settings", invoiceSettingsParams);

                    Customer customer = Customer.retrieve(invoice.getCustomer()).update(customerParams);
                    PaymentMethod paymentMethod = PaymentMethod.retrieve(customer.getInvoiceSettings().getDefaultPaymentMethod());
                    subscriptionDetail.setCardLastFourDigits(paymentMethod.getCard().getLast4());
                    subscriptionDetail.setCardType(paymentMethod.getCard().getBrand());
                } catch (StripeException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice payment failed");
                }
                handleSubscriptionPaid(subscriptionDetail);
            }
            case "customer.subscription.deleted" -> {
                status = "Subscription Deleted";
                handleSubscriptionDeleted((Subscription) stripeObject, subscriptionDetail);
            }
            case "customer.subscription.created", "customer.subscription.updated" -> {
                status = "Subscription Created/Updated";
                handleSubscriptionCreatedOrUpdated((Subscription) stripeObject, subscriptionDetail);
            }
            default -> status = "Unhandled event type: " + event.getType();
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", status);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<Map<String, String>> createPortalSession(JwtAuthenticationToken principal) {
        // Creates customer portal session where the customer can manage his subscription
        AppUser user = jwtService.retrieveUserFromJwt(principal);

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

    private void handleSubscriptionPaid(SubscriptionDetail subscriptionDetail) {
        // Reset limit Shipment creation
        subscriptionDetail.setCurrentLimit(0);
        subscriptionDetail.setLastPaymentDate(Instant.now().getEpochSecond());
        subscriptionDetailService.modifySubscriptionDetail(subscriptionDetail);
    }

    private SubscriptionDetail handleSubscriptionCreatedOrUpdated(Subscription subscription, SubscriptionDetail subscriptionDetail) {
        // Provision user to correct role and subscription plan
        AppUser user = subscriptionDetail.getUser();
        if (subscriptionDetail.getSubscriptionId() == null || !Objects.equals(subscription.getId(), subscriptionDetail.getSubscriptionId())) {
            // User subscribed to plan for the first time or to a different plan
            subscriptionDetail.setStartDate(subscription.getStartDate());
        }
        user.setRole(AppUserRoleEnum.USER);
        subscriptionDetail.setSubscriptionId(subscription.getId());
        String priceId = subscription.getItems().getData().get(0).getPlan().getId();
        subscriptionDetail.setSubscriptionPlan(subscriptionPlanService.retrieveSubscriptionPlanByPriceId(priceId));
        subscriptionDetail.setEndDate(null);
        // Will persist user as well as SubscriptionDetail
        return subscriptionDetailService.modifySubscriptionDetail(subscriptionDetail);
    }

    private void handleSubscriptionDeleted(Subscription subscription, SubscriptionDetail subscriptionDetail) {
        AppUser user = subscriptionDetail.getUser();
        // Still has access to platform to see shipments or renew subscription, but subscription is dead, and he can no longer create shipments
        user.setRole(AppUserRoleEnum.UNPAID_USER);
        subscriptionDetail.setSubscriptionId(null);
        subscriptionDetail.setSubscriptionPlan(null);
        subscriptionDetail.setEndDate(subscription.getEndedAt());
        subscriptionDetailService.modifySubscriptionDetail(subscriptionDetail);
    }
}
