package com.navaship.api.shipments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListShipmentsResponse {
    private int currentPage = 1;
    private int totalPages = 1;
    private List<ShipmentResponse> data;
}
