package com.navaship.api.dashboard;

import com.navaship.api.activity.ActivityLogResponse;
import com.navaship.api.activity.ActivityLoggerService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.easypost.EasypostShipmentStatus;
import com.navaship.api.jwt.JwtService;
import com.navaship.api.packages.PackageService;
import com.navaship.api.shipment.ShipmentService;
import com.navaship.api.shipment.ShipmentStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/dashboard")
public class DashboardController {
    private JwtService jwtService;
    private ShipmentService shipmentService;
    private PackageService packageService;
    private ActivityLoggerService activityLoggerService;


    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> retrieveDashboardStatistics(JwtAuthenticationToken principal) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        int totalShipmentsCreatedCount = shipmentService.retrieveUserShipmentsCount(user);
        int totalShipmentsInTransitCount = shipmentService.retrieveUserShipmentsCountByEasypostStatus(user, EasypostShipmentStatus.IN_TRANSIT);
        int totalShipmentsDeliveredCount = shipmentService.retrieveUserShipmentsCountByEasypostStatus(user, EasypostShipmentStatus.DELIVERED);
        int totalShipmentsDraftCount = shipmentService.retrieveUserShipmentsCountByStatus(user, ShipmentStatus.DRAFT);
        int totalPackagesCount = packageService.retrieveUserPackagesCount(user);

        BigDecimal totalMoneySaved = shipmentService.getTotalMoneySaved(user);
        if (totalMoneySaved.compareTo(BigDecimal.ZERO) < 0) {
            totalMoneySaved = BigDecimal.ZERO;
        }

        int currentMonthShipmentCreated = user.getSubscriptionDetail().getCurrentLimit();
        int maxShipmentCreatedLimit = user.getSubscriptionDetail().getSubscriptionPlan().getMaxLimit();

        List<ActivityLogResponse> activityLogs = activityLoggerService.convertToActivityLogResponse(activityLoggerService.findLatestActivityLogs(user));
        return new ResponseEntity<>(
                new DashboardResponse(
                        totalShipmentsCreatedCount,
                        totalShipmentsInTransitCount,
                        totalShipmentsDeliveredCount,
                        totalShipmentsDraftCount,
                        totalPackagesCount,
                        totalMoneySaved,
                        currentMonthShipmentCreated,
                        maxShipmentCreatedLimit,
                        activityLogs
                ),
                HttpStatus.OK
        );
    }
}
