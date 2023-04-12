package com.navaship.api.shipment;

import com.navaship.api.easypost.EasypostShipmentStatus;
import com.navaship.api.person.PersonResponse;
import com.navaship.api.rate.RateResponse;
import com.navaship.api.shipmentaddress.ShipmentAddressResponse;
import com.navaship.api.shipmentpackage.ShipmentPackageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String easypostShipmentId;
    private List<ShipmentAddressResponse> addresses;
    private ShipmentPackageResponse parcel;
    private ShipmentStatus status;
    private EasypostShipmentStatus easyPostStatus;
    private RateResponse rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingCode;
    private String postageLabelUrl;
    private String publicTrackingUrl;
    private List<PersonResponse> persons;
    private boolean isInsured;
    private BigDecimal insuranceAmount;
    private Integer shipmentNumber;
}
