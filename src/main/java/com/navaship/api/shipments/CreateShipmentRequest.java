package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateShipmentRequest {
    @NotNull
    Long fromAddressId;
    @NotNull
    Long toAddressId;
    @NotNull
    Long parcelId;
}
