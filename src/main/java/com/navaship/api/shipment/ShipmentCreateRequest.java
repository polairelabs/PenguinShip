package com.navaship.api.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentCreateRequest {
    @NotNull
    private Long fromAddressId;
    @NotNull
    private Long toAddressId;
    @NotNull
    private Long parcelId;
    @Size(max = 60)
    private String senderName;
    @Size(max = 60)
    private String senderCompany;
    @Size(max = 20)
    private String senderPhone;
    @Size(max = 60)
    private String senderEmail;
    @Size(max = 60)
    private String receiverName;
    @Size(max = 60)
    private String receiverCompany;
    @Size(max = 20)
    private String receiverPhone;
    @Size(max = 60)
    private String receiverEmail;
}
