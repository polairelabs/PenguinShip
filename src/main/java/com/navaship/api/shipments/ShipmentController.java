package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.navaship.api.addresses.Address;
import com.navaship.api.addresses.AddressService;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/shipments")
public class ShipmentController {
    private final EasyPostService easyPostService;
    private final AddressService addressService;
    private final PackageService packageService;

    @PostMapping()
    private ResponseEntity<String> createShipments(@RequestParam Long fromAddressId,
                                                   @RequestParam Long toAddressId,
                                                   @RequestParam Long parcelId) {
        // TODO check fromAddressId and toAddressId cannot be the same (elementary check)
        Address fromAddress = addressService.retrieveAddress(fromAddressId);
        Address toAddress = addressService.retrieveAddress(toAddressId);
        Package parcel = packageService.retrievePackage(parcelId);

        try {
            easyPostService.createShipment(fromAddress, toAddress, parcel);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
