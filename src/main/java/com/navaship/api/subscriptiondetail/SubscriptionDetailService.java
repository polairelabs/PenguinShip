package com.navaship.api.subscriptiondetail;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class SubscriptionDetailService {
    private SubscriptionDetailRepository subscriptionDetailRepository;


    public SubscriptionDetail createSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        return subscriptionDetailRepository.save(subscriptionDetail);
    }

    public SubscriptionDetail updateSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        return subscriptionDetailRepository.save(subscriptionDetail);
    }

    public SubscriptionDetail retrieveSubscriptionDetail(String stripeCustomerId) {
        return subscriptionDetailRepository.findSubscriptionDetailByStripeCustomerId(stripeCustomerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stripe customer not found")
        );
    }
}
