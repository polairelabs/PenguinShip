package com.navaship.api.rate;

import com.navaship.api.subscription.SubscriptionPlan;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RateService {
    private final RateRepository rateRepository;
    private final ModelMapper modelMapper;


    public RateService(RateRepository rateRepository, ModelMapper modelMapper) {
        this.rateRepository = rateRepository;
        this.modelMapper = modelMapper;
    }

    public Rate createRate(Rate rate) {
        return rateRepository.save(rate);
    }

    public BigDecimal calculateRate(com.easypost.model.Rate rate, SubscriptionPlan subscriptionPlan) {
        // Calculate overhead % on top of Rate
        BigDecimal rateValue = new BigDecimal(Float.toString(rate.getRate()));
        BigDecimal serviceFee = subscriptionPlan.getShipmentHandlingFee();
        if (serviceFee.compareTo(BigDecimal.ZERO) == 0) {
            return rateValue.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal additionalOverheadFee = rateValue.multiply(serviceFee);
        return rateValue.add(additionalOverheadFee).setScale(2, RoundingMode.HALF_UP);
    }

    public Rate convertToRate(com.easypost.model.Rate rate) {
        return modelMapper.map(rate, Rate.class);
    }

    public RateResponse convertToRateResponse(Rate rate) {
        return modelMapper.map(rate, RateResponse.class);
    }
}
