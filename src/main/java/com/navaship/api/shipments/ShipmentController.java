package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Shipment;
import com.navaship.api.addresses.Address;
import com.navaship.api.addresses.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/shipments")
public class ShipmentController {
    private final EasyPostService easyPostService;
    private final AddressService addressService;
    private final PackageService packageService;
    private final ShipmentService shipmentService;
    private final AppUserService appUserService;


    @GetMapping
    public List<com.navaship.api.shipments.Shipment> listShipments() {
        return shipmentService.getAllShipments();
    }

    @PostMapping()
    public ResponseEntity<Shipment> createShipment(JwtAuthenticationToken principal,
                                                    @RequestParam Long fromAddressId,
                                                    @RequestParam Long toAddressId,
                                                    @RequestParam Long parcelId) {
        // TODO check fromAddressId and toAddressId cannot be the same (elementary check)
        AppUser user = retrieveUserFromJwt(principal);
        Address fromAddress = addressService.retrieveAddress(fromAddressId);
        Address toAddress = addressService.retrieveAddress(toAddressId);
        Package parcel = packageService.retrievePackage(parcelId);

        Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);

            com.navaship.api.shipments.Shipment navaShipment = new com.navaship.api.shipments.Shipment();
            navaShipment.setEasypostShipmentId(shipment.getId());
            shipmentService.saveShipment(navaShipment, user, fromAddress, toAddress, parcel);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(shipment, HttpStatus.OK);
    }

    @PostMapping("/buy/{easypostShipmentId}")
    public ResponseEntity<Shipment> buyShipment(@PathVariable String easypostShipmentId,
                                               @RequestParam String rate) {
        Shipment shipment = null;
        try {
            shipment = easyPostService.buyShipment(easypostShipmentId, rate);
            // TODO create rate here
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(shipment, HttpStatus.OK);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }
}
