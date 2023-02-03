package com.navaship.api.rates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RateResponse {
    private String id; // easypost rate id
    private String carrier;
    private String service;
    private String serviceCode;
    private BigDecimal rate;
    private String currency;
    private BigDecimal listRate;
    private String listCurrency;
    private BigDecimal retailRate;
    private String retailCurrency;
    private String deliveryDays;
    private String deliveryDate;
    private Boolean deliveryDateGuaranteed;
    private String estDeliveryDays;
    private String carrierAccountId;
    private String billingType;
}