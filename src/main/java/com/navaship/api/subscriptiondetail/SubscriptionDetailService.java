package com.navaship.api.subscriptiondetail;

import com.navaship.api.appuser.AppUser;
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

    public SubscriptionDetail modifySubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        return subscriptionDetailRepository.save(subscriptionDetail);
    }

    public SubscriptionDetail retrieveSubscriptionDetail(String stripeCustomerId) {
        return subscriptionDetailRepository.findSubscriptionDetailByStripeCustomerId(stripeCustomerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stripe customer not found")
        );
    }

    public void deleteSubscriptionDetail(AppUser user) {
        subscriptionDetailRepository.deleteById(user.getId());
    }
}
