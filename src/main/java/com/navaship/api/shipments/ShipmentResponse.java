package com.navaship.api.shipments;

import com.navaship.api.addresses.AddressResponse;
import com.navaship.api.packages.PackageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String easypostShipmentId;
    private AddressResponse toAddress;
    private AddressResponse fromAddress;
    private PackageResponse parcel;
    private ShipmentStatus shipmentStatus;
    private RateResponse rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingCode;
    private String postageLabelUrl;
    private String easypostShipmentStatus;
    private String publicTrackingUrl;
    private String additionalInfoJson;
}
