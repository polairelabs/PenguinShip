package com.navaship.api.shipments;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.navaship.api.addresses.Address;
import com.navaship.api.packages.Package;
import com.navaship.api.rates.NavaRate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BuyShipmentResponse {
    private Long id;
    private String easypostShipmentId;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Address toAddress;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Address fromAddress;
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Package parcel;
    private NavaRate rate;
    private ShipmentStatus status;
    private String trackingCode;
    private String postageLabelUrl;
    private String easypostShipmentStatus;
    private String publicTrackingUrl;
    private String additionalInfoJson;
}
