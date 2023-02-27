package com.navaship.api.rate;

import com.navaship.api.subscription.SubscriptionPlan;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@Service
public class RateService {
    public static final double INSURANCE_FEE_PERCENTAGE = 0.5;
    private final RateRepository rateRepository;
    private final ModelMapper modelMapper;


    public RateService(RateRepository rateRepository, ModelMapper modelMapper) {
        this.rateRepository = rateRepository;
        this.modelMapper = modelMapper;
        Converter<Rate, String> rateToStringConverter = new Converter<Rate, String>() {
            public String convert(MappingContext<Rate, String> context) {
                return new DecimalFormat("0.00").format(context.getSource().getRate());
            }
        };
        this.modelMapper.addConverter(rateToStringConverter);
    }

    public Rate createRate(Rate rate) {
        return rateRepository.save(rate);
    }

    public BigDecimal calculateRate(com.easypost.model.Rate rate, SubscriptionPlan subscriptionPlan) {
        // Calculate overhead % on top of Rate
        BigDecimal rateValue = new BigDecimal(Float.toString(rate.getRate()));
        BigDecimal serviceFee = subscriptionPlan.getShipmentHandlingFee();
        if (serviceFee.compareTo(BigDecimal.ZERO) > 0) {
            // Add overhead when service fee is not equal or less than 0
            BigDecimal additionalOverheadFee = rateValue.multiply(serviceFee);
            return rateValue.add(additionalOverheadFee).setScale(2, RoundingMode.HALF_UP);
        }
        return rateValue.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateInsuranceFee(BigDecimal insuranceAmount) {
        BigDecimal insuranceFeePercentage = new BigDecimal(INSURANCE_FEE_PERCENTAGE / 100);
        return insuranceAmount.multiply(insuranceFeePercentage).setScale(2, RoundingMode.HALF_UP);
    }

    public Rate convertToRate(com.easypost.model.Rate rate) {
        return modelMapper.map(rate, Rate.class);
    }

    public RateResponse convertToRateResponse(Rate rate) {
        return modelMapper.map(rate, RateResponse.class);
    }
}
