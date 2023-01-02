package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentCreatedResponse {
    // easypost shipment id
    private String id;
    private ArrayList<RateResponse> rates;
}
