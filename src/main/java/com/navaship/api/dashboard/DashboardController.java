package com.navaship.api.dashboard;

import com.navaship.api.address.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.jwt.JwtService;
import com.navaship.api.packages.PackageService;
import com.navaship.api.shipment.ShipmentService;
import com.navaship.api.shipment.ShipmentStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/dashboard")
public class DashboardController {
    private JwtService jwtService;
    private ShipmentService shipmentService;
    private PackageService packageService;


    @PostMapping
    public ResponseEntity<Object> retrieveDashboardStatistics(JwtAuthenticationToken principal) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        int totalUserShipmentsCount = shipmentService.retrieveUserShipmentsCount(user);
        int totalUserLabelsPurchasedCount = shipmentService.retrieveUserShipmentsCountByStatus(user, ShipmentStatus.PURCHASED);
        int totalUserPackages = packageService.retrieveUserPackagesCount(user);

        
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
