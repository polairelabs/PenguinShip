package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Rate;
import com.easypost.model.Shipment;
import com.navaship.api.addresses.Address;
import com.navaship.api.addresses.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import com.navaship.api.rates.NavaRate;
import com.navaship.api.rates.RateService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/shipments")
public class ShipmentController {
    private EasyPostService easyPostService;
    private ShipmentService shipmentService;
    private AddressService addressService;
    private PackageService packageService;
    private AppUserService appUserService;
    private RateService rateService;


    @GetMapping
    public List<NavaShipment> listShipments(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        return shipmentService.findAllShipments(user);
    }

    @PostMapping()
    public ResponseEntity<Shipment> createShipment(JwtAuthenticationToken principal,
                                                   @RequestParam Long fromAddressId,
                                                   @RequestParam Long toAddressId,
                                                   @RequestParam Long parcelId) {
        // TODO check fromAddressId and toAddressId cannot be the same (elementary check)
        // TODO check if all resources belong to user
        AppUser user = retrieveUserFromJwt(principal);
        Address fromAddress = addressService.retrieveAddress(fromAddressId);
        Address toAddress = addressService.retrieveAddress(toAddressId);
        Package parcel = packageService.retrievePackage(parcelId);

        Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);

            NavaShipment navaShipment = new NavaShipment();
            navaShipment.setEasypostShipmentId(shipment.getId());
            shipmentService.createShipment(navaShipment, user, fromAddress, toAddress, parcel);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(shipment, HttpStatus.OK);
    }

    @PostMapping("/buy/{shipmentId}")
    public ResponseEntity<NavaShipmentResponse> buyShipment(@PathVariable Long shipmentId, @Valid @RequestBody BuyShipmentRateRequest buyShipmentRateRequest) {
        NavaShipment navaShipment = shipmentService.retrieveShipment(shipmentId);
        // TODO check if resource belongs to user, even if we decide to use easypostId which is a UUID
        // We still don't want the a logged in user to access another one's resource if he doesn't own the resource
        // TODO Check the passed easypostShipmentId is the correct id associated with NavaShipment
        try {
            Map<String, Object> results = easyPostService.buyShipmentRate(
                    buyShipmentRateRequest.getEasypostShipmentId(),
                    buyShipmentRateRequest.getEasypostRateId()
            );

            Shipment shipment = (Shipment) results.get("boughtShipment");
            Rate rate = (Rate) results.get("boughtRate");

//            NavaRate navaRate = new NavaRate();
//            navaRate.setId(rate.getId());
//            navaRate.setCarrier(rate.getCarrier());
//            navaRate.setCarrier(rate.getCarrier());
//            navaRate.setRate(BigDecimal.valueOf(rate.getRate()));
//            navaRate.setCurrency(rate.getCurrency());
//            navaRate.setDeliveryDays(0);
//            navaRate.setEstDeliveryDays(0);
//            navaRate.setDeliveryDateGuaranteed(rate.getDeliveryDateGuaranteed());

            // Modify shipment with new attributes trackingCode and labelUrl
            navaShipment.setTrackingCode(shipment.getTrackingCode());
            navaShipment.setPostageLabelUrl(shipment.getPostageLabel().getLabelUrl());
            navaShipment.setStatus(ShipmentStatus.PURCHASED);

            NavaRate navaRate = rateService.convertToNavaRate(rate);
            rateService.createRate(navaRate);
            // Set the bought rate to the shipment
            navaShipment.setRate(navaRate);

            // Update Shipment
            shipmentService.modifyShipment(navaShipment);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(
                shipmentService.convertToNavaShipmentResponse(navaShipment),
                HttpStatus.OK);
    }

    @GetMapping("/rates/{shipmentId}")
    public ResponseEntity<List<Rate>> getRates(@PathVariable Long shipmentId) {
        NavaShipment navaShipment = shipmentService.retrieveShipment(shipmentId);
        List<Rate> rates = new ArrayList<>();
        try {
            rates = easyPostService.getShipmentRates(navaShipment.getEasypostShipmentId());
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(rates, HttpStatus.OK);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }
}
