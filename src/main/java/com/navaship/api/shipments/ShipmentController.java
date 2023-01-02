package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Rate;
import com.easypost.model.Shipment;
import com.google.gson.JsonObject;
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
import java.util.Objects;

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
    public ResponseEntity<List<ShipmentResponse>> getAllUserShipments(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        List<ShipmentResponse> shipments = shipmentService.findAllShipments(user)
                .stream().map(shipmentService::convertToShipmentResponse)
                .toList();
        return new ResponseEntity<>(shipments, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ShipmentCreatedResponse> createShipment(JwtAuthenticationToken principal,
                                                                  @Valid @RequestBody CreateShipmentRequest createShipmentRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Address fromAddress = addressService.retrieveAddress(createShipmentRequest.fromAddressId);
        checkAddressBelongsToUser(principal, fromAddress);

        Address toAddress = addressService.retrieveAddress(createShipmentRequest.toAddressId);
        checkAddressBelongsToUser(principal, toAddress);

        Package parcel = packageService.retrievePackage(createShipmentRequest.parcelId);
        checkParcelBelongsToUser(principal, parcel);

        if (Objects.equals(fromAddress.getId(), toAddress.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and delivery address cannot be the same");
        }

        fromAddress.setName(createShipmentRequest.senderName);
        fromAddress.setCompany(createShipmentRequest.senderCompany);
        fromAddress.setPhone(createShipmentRequest.senderPhone);
        fromAddress.setEmail(createShipmentRequest.senderEmail);

        toAddress.setName(createShipmentRequest.receiverName);
        toAddress.setCompany(createShipmentRequest.receiverCompany);
        toAddress.setPhone(createShipmentRequest.receiverPhone);
        toAddress.setEmail(createShipmentRequest.receiverEmail);

        JsonObject additionalInfo = new JsonObject();
        additionalInfo.add("sender", fromAddress.additionalInfoToJson());
        additionalInfo.add("receiver", toAddress.additionalInfoToJson());

        Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);

            NavaShipment navaShipment = new NavaShipment();
            navaShipment.setEasypostShipmentId(shipment.getId());
            navaShipment.setStatus(ShipmentStatus.DRAFT);
            shipmentService.createShipment(navaShipment, user, fromAddress, toAddress, parcel, additionalInfo.toString());
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        // A rates array is return with ShipmentCreatedResponse object
        ShipmentCreatedResponse shipmentCreatedResponse = shipmentService.convertToShipmentCreateResponse(shipment);
        return new ResponseEntity<>(shipmentCreatedResponse, HttpStatus.OK);
    }

    @PostMapping("/buy")
    public ResponseEntity<BuyShipmentResponse> buyShipmentRate(JwtAuthenticationToken principal,
                                                               @Valid @RequestBody BuyRateRequest buyRateRequest) {
        NavaShipment navaShipment = shipmentService.retrieveShipment(buyRateRequest.getEasypostShipmentId());
        checkNavaShipmentBelongsToUser(principal, navaShipment);

        try {
            Shipment shipment = easyPostService.buyShipmentRate(
                    buyRateRequest.getEasypostShipmentId(),
                    buyRateRequest.getEasypostRateId()
            );

            Rate rate = shipment.getSelectedRate();

            // Modify shipment with new attributes trackingCode and labelUrl
            navaShipment.setTrackingCode(shipment.getTrackingCode());
            if (shipment.getPostageLabel() != null) {
                navaShipment.setPostageLabelUrl(shipment.getPostageLabel().getLabelUrl());
            }
            if (shipment.getTracker() != null) {
                navaShipment.setPublicTrackingUrl(shipment.getTracker().getPublicUrl());
            }
            navaShipment.setEasypostShipmentStatus(shipment.getStatus());
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
                shipmentService.convertToBuyShipmentResponse(navaShipment),
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

    private void checkNavaShipmentBelongsToUser(JwtAuthenticationToken principal,
                                                NavaShipment navaShipment) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        if (!navaShipment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }

    private void checkAddressBelongsToUser(JwtAuthenticationToken principal,
                                           Address address) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }

    private void checkParcelBelongsToUser(JwtAuthenticationToken principal,
                                          Package parcel) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        if (!parcel.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }
}
