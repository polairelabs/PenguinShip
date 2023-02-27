package com.navaship.api.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ShipmentBuyRateRequest {
    @NotBlank
    @NotNull
    private String easypostShipmentId;
    @NotBlank
    @NotNull
    private String easypostRateId;
    @NotNull
    private Boolean isInsured;
    @Digits(integer = 5, fraction = 2, message = "Only two decimal points are allowed")
    @DecimalMin(value = "0.01", message = "Value must be greater than zero")
    private BigDecimal insuranceAmount;

}
