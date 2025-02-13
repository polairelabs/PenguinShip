package com.navaship.api.shipment;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.navaship.api.packages.Package;
import com.navaship.api.rate.RateResponse;
import com.navaship.api.shipmentaddress.ShipmentAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentBoughtResponse {
    private Long id;
    private String easypostShipmentId;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private ShipmentAddress toAddress;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private ShipmentAddress fromAddress;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Package parcel;
    private RateResponse rate;
    private ShipmentStatus status;
    private String trackingCode;
    private String postageLabelUrl;
    private String easypostShipmentStatus;
}
