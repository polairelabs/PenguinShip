package com.navaship.api.activity;

import com.navaship.api.easypost.EasypostShipmentStatus;
import com.navaship.api.shipment.ShipmentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogResponse {
    private String message;
    private ActivityMessageType messageType;
    private ShipmentResponse shipment;
    private LocalDateTime createdAt;
    private EasypostShipmentStatus easypostStatus;
}
