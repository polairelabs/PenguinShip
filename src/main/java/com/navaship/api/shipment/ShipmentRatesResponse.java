package com.navaship.api.shipment;

import com.navaship.api.rate.RateResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentRatesResponse {
    private String id; // easypost shipment id
    private List<RateResponse> rates;
}
