package com.navaship.api.subscription;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SubscriptionPlanService {
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private ModelMapper modelMapper;


    public List<SubscriptionPlan> retrieveSubscriptionPlans() {
        return subscriptionPlanRepository.findAll();
    }

    public SubscriptionPlan createSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        return subscriptionPlanRepository.save(subscriptionPlan);
    }

    public SubscriptionPlanResponse convertToSubscriptionPlanResponse(SubscriptionPlan subscriptionPlan) {
        return modelMapper.map(subscriptionPlan, SubscriptionPlanResponse.class);
    }
}
