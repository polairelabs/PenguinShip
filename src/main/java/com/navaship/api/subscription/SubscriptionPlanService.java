package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@AllArgsConstructor
public class SubscriptionPlanService {
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private ModelMapper modelMapper;


    public List<SubscriptionPlan> retrieveSubscriptionPlans() {
        return subscriptionPlanRepository.findAll();
    }

    public SubscriptionPlan retrieveSubscriptionPlan(Long id) {
        return subscriptionPlanRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found")
        );
    }

    public SubscriptionPlan retrieveSubscriptionPlanByPriceId(String stripePriceId) {
        return subscriptionPlanRepository.findSubscriptionPlanByStripePriceId(stripePriceId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found")
        );
    }

    public SubscriptionPlan createSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        return subscriptionPlanRepository.save(subscriptionPlan);
    }

    public SubscriptionPlan modifySubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        return subscriptionPlanRepository.save(subscriptionPlan);
    }
    public BigDecimal calculateRoundedHandlingFee(BigDecimal handlingFeePercentage) {
        return handlingFeePercentage.divide(new BigDecimal("100"), 3, RoundingMode.DOWN);
    }

    public SubscriptionPlanResponse convertToSubscriptionPlanResponse(SubscriptionPlan subscriptionPlan) {
        return modelMapper.map(subscriptionPlan, SubscriptionPlanResponse.class);
    }

    public SubscriptionPlanUpdateResponse convertToSubscriptionPlanUpdateResponse(SubscriptionPlan subscriptionPlan) {
        return modelMapper.map(subscriptionPlan, SubscriptionPlanUpdateResponse.class);
    }

    public SubscriptionPlan convertToSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest) {
        return modelMapper.map(subscriptionPlanRequest, SubscriptionPlan.class);
    }
}
