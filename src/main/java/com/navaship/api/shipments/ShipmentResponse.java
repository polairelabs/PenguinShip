package com.navaship.api.shipments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navaship.api.addresses.AddressResponse;
import com.navaship.api.packages.PackageResponse;
import com.navaship.api.person.PersonResponse;
import com.navaship.api.rates.RateResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    @JsonProperty("navashipShipmentStatus")
    private ShipmentStatus status;
    private ShipmentStatusEasyPost easypostShipmentStatusEasyPost;
    private RateResponse rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingCode;
    private String postageLabelUrl;
    private String publicTrackingUrl;
    private List<PersonResponse> persons;
}
