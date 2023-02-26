package com.navaship.api.shipment;

import com.navaship.api.validators.BigDecimalLength;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.DecimalMax;
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
    @DecimalMax(value = "5000.00")
    @BigDecimalLength(maxLength = 5)
    private BigDecimal insuranceAmount;

}
