package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentAddressResponse {
    private Long id;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private Boolean residential;
    private ShipmentAddressType type;
}
