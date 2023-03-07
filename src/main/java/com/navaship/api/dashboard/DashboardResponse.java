package com.navaship.api.dashboard;

import com.navaship.api.activity.ActivityLogResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardResponse {
    int totalShipmentsCreatedCount;
    int totalShipmentsInTransitCount;
    int totalShipmentsDeliveredCount;
    int totalShipmentsDraftCount;
    int totalPackagesCount;
    BigDecimal totalMoneySaved;
    int currentMonthShipmentCreated;
    int maxShipmentCreatedLimit;
    List<ActivityLogResponse> logs;
}
