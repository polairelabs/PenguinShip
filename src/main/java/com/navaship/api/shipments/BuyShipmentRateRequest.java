package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class BuyShipmentRateRequest {
    @NotBlank
    @NotNull
    private String easypostShipmentId;
    @NotBlank
    @NotNull
    private String easypostRateId;
}
