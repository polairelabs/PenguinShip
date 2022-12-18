package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    // from
    @Size(max = 60)
    String senderName;
    @Size(max = 60)
    String senderCompany;
    @Size(max = 20)
    String senderPhone;
    @Size(max = 60)
    String senderEmail;
    // to
    @Size(max = 60)
    String receiverName;
    @Size(max = 60)
    String receiverCompany;
    @Size(max = 18)
    String receiverPhone;
    @Size(max = 60)
    String receiverEmail;
}
