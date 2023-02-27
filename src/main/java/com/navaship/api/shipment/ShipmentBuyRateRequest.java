package com.navaship.api.shipment;

import com.navaship.api.validators.BigDecimalLength;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.DecimalMin;
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
    @BigDecimalLength(maxLength = 6)
    @DecimalMin(value = "0.01", message = "Value must be greater than zero")
    private BigDecimal insuranceAmount;

}
