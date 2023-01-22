package com.navaship.api.subscriptiondetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionDetailRepository extends JpaRepository<SubscriptionDetail, Long> {
    Optional<SubscriptionDetail> findSubscriptionDetailByStripeCustomerId(String stripeCustomerId);
}