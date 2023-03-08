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
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/admin")
public class AdminController {
    private SubscriptionPlanService subscriptionPlanService;
    private StripeService stripeService;


    @PutMapping("/subscriptions/{subscriptionPlanId}")
    public ResponseEntity<SubscriptionPlanUpdateResponse> updateSubscriptionPlan(@PathVariable Long subscriptionPlanId,
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
        BigDecimal shipmentHandlingFee = subscriptionPlanService.calculateRoundedHandlingFee(subscriptionPlanRequest.getShipmentHandlingFee());

        SubscriptionPlan convertedSubscriptionPlan = subscriptionPlanService.convertToSubscriptionPlan(subscriptionPlanRequest);
        convertedSubscriptionPlan.setId(subscriptionPlan.getId());
        convertedSubscriptionPlan.setShipmentHandlingFee(shipmentHandlingFee);

        SubscriptionPlan updatedSubscriptionPlan = subscriptionPlanService.modifySubscriptionPlan(convertedSubscriptionPlan);
        return new ResponseEntity<>(subscriptionPlanService.convertToSubscriptionPlanUpdateResponse(updatedSubscriptionPlan), HttpStatus.OK);
    }
}
