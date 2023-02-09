package com.navaship.api.shipments;

import com.navaship.api.rates.RateResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentCreatedResponse {
    private String id; // easypost shipment id
    private List<RateResponse> rates;
}
