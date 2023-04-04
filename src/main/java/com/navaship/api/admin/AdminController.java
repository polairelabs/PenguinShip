package com.navaship.api.admin;

import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/admin")
public class AdminController {
    private SubscriptionPlanService subscriptionPlanService;
    private StripeService stripeService;


    @PutMapping("/subscriptions/{subscriptionPlanId}")
    public ResponseEntity<SubscriptionPlanUpdateResponse> updateSubscriptionPlan(@PathVariable String subscriptionPlanId,
                                                                                 @Valid @RequestBody SubscriptionPlanRequest subscriptionPlanRequest) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanService.retrieveSubscriptionPlan(subscriptionPlanId);
        boolean isPriceFound;
        try {
            List<Price> prices = stripeService.retrievePrices();
            isPriceFound = prices.stream().anyMatch(price -> Objects.equals(price.getId(), subscriptionPlanRequest.getStripePriceId()));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving products");
        }

        if (!isPriceFound) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided StripePriceId is invalid. No product with that id found. Make sure to create the product in Stripe first");
        }

        // The ShipmentHandlingFee passed in the request is in percentage (1% to 100%)
        BigDecimal shipmentHandlingFee = subscriptionPlanService.calculateRoundedHandlingFee(subscriptionPlanRequest.getHandlingFeePercentage());

        SubscriptionPlan convertedSubscriptionPlan = subscriptionPlanService.convertToSubscriptionPlan(subscriptionPlanRequest);
        convertedSubscriptionPlan.setId(subscriptionPlan.getId());
        convertedSubscriptionPlan.setShipmentHandlingFee(shipmentHandlingFee);
        SubscriptionPlan updatedSubscriptionPlan = subscriptionPlanService.updateSubscriptionPlan(convertedSubscriptionPlan);

        return new ResponseEntity<>(subscriptionPlanService.convertToSubscriptionPlanUpdateResponse(updatedSubscriptionPlan), HttpStatus.OK);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionPlanAdminResponse>> retrieveSubscriptions() {
        // Used to retrieve membership details for admin (price id is better off hidden from users)
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.retrieveSubscriptionPlans();
        List<SubscriptionPlanAdminResponse> subscriptionPlanResponses = new ArrayList<>();
        List<SubscriptionPlanAdminResponse> sortedSubscriptionPlans = new ArrayList<>();
        try {
            for (SubscriptionPlan subscriptionPlan : subscriptionPlans) {
                SubscriptionPlanAdminResponse subscriptionPlanAdminResponse = subscriptionPlanService.convertToSubscriptionPlanAdminResponse(subscriptionPlan);
                Price price = stripeService.retrievePrice(subscriptionPlan.getStripePriceId());
                subscriptionPlanAdminResponse.setCurrency(price.getCurrency());
                subscriptionPlanAdminResponse.setUnitAmount(price.getUnitAmount());
                subscriptionPlanAdminResponse.setShipmentHandlingFee(subscriptionPlan.getShipmentHandlingFee());
                subscriptionPlanAdminResponse.setStripePriceId(subscriptionPlan.getStripePriceId());
                subscriptionPlanResponses.add(subscriptionPlanAdminResponse);
                sortedSubscriptionPlans = subscriptionPlanResponses.stream()
                        .sorted(Comparator.comparingLong(SubscriptionPlanResponse::getUnitAmount)).toList();
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Retrieving memberships error");
        }

        return new ResponseEntity<>(sortedSubscriptionPlans, HttpStatus.OK);
    }

    @GetMapping("/subscriptions/prices")
    public ResponseEntity<List<String>> retrieveStripPriceIds() {
        List<String> stripePriceIds = new ArrayList<>();
        try {
            for (Price price : stripeService.retrievePrices()) {
                stripePriceIds.add(price.getId());
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Retrieving Stripe prices error");
        }

        return new ResponseEntity<>(stripePriceIds, HttpStatus.OK);
    }
}
