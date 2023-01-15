package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Event;
import com.google.gson.JsonObject;
import com.navaship.api.addresses.Address;
import com.navaship.api.addresses.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import com.navaship.api.rates.Rate;
import com.navaship.api.rates.RateService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

import static com.navaship.api.common.ListApiConstants.*;
import static com.navaship.api.common.ListApiConstants.DEFAULT_PAGE_SIZE;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/shipments")
public class ShipmentController {
    public static final int DEFAULT_SIZE = 50;
    private EasyPostService easyPostService;
    private ShipmentService shipmentService;
    private AddressService addressService;
    private PackageService packageService;
    private AppUserService appUserService;
    private RateService rateService;


    @GetMapping
    public ResponseEntity<ListApiResponse<ShipmentResponse>> getAllUserShipments(JwtAuthenticationToken principal,
                                                                                @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER + "") int pageNumber,
                                                                                @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                                @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                                @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = retrieveUserFromJwt(principal);
        ListApiResponse<ShipmentResponse> listApiResponse = new ListApiResponse<>();

        // Validate page size
        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Decrement page number to match zero-based index
        int zeroBasedPageNumber = pageNumber - 1;

        try {
            // Retrieve addresses with pagination
            Page<Shipment> shipmentsWithPagination = shipmentService.findAllShipments(user, zeroBasedPageNumber, pageSize, sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
            listApiResponse.setData(shipmentsWithPagination.map(shipmentService::convertToShipmentResponse).toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        // Calculate total pages
        int totalPages = (int) Math.round(shipmentService.retrieveUserShipmentsCount(user) / (double) pageSize);
        listApiResponse.setTotalPages(totalPages);
        listApiResponse.setCount(listApiResponse.getData().size());
        listApiResponse.setCurrentPage(zeroBasedPageNumber + 1);

        return new ResponseEntity<>(listApiResponse, HttpStatus.OK);
    }

    @PostMapping("/easypost-webhook")
    public ResponseEntity<String> easyPostWebhook(@RequestBody byte[] eventBody, HttpServletRequest request) {
        // Webhook to listen for events and update shipments from easypost
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Hmac-Signature", request.getHeader("X-Hmac-Signature"));
        try {
            Event event = easyPostService.validateWebhook(eventBody, headers);
            // Do something with the event
            return new ResponseEntity<>("Webhook event received and handled, current status " + event.getStatus(), HttpStatus.OK);
        } catch (EasyPostException e) {
            return new ResponseEntity<>("Invalid webhook signature", HttpStatus.UNAUTHORIZED);
        }
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

        com.easypost.model.Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);

            Shipment navaShipment = new Shipment();
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
        Shipment navaShipment = shipmentService.retrieveShipment(buyRateRequest.getEasypostShipmentId());
        checkNavaShipmentBelongsToUser(principal, navaShipment);

        try {
            com.easypost.model.Shipment shipment = easyPostService.buyShipmentRate(
                    buyRateRequest.getEasypostShipmentId(),
                    buyRateRequest.getEasypostRateId()
            );

            navaShipment.setStatus(ShipmentStatus.PURCHASED);
            com.easypost.model.Rate rate = shipment.getSelectedRate();

            // Modify shipment with new generated (from easypost) attributes trackingCode and labelUrl
            navaShipment.setTrackingCode(shipment.getTrackingCode());
            if (shipment.getPostageLabel() != null) {
                navaShipment.setPostageLabelUrl(shipment.getPostageLabel().getLabelUrl());
            }

            if (shipment.getTracker() != null) {
                navaShipment.setPublicTrackingUrl(shipment.getTracker().getPublicUrl());
            }

            Rate navaRate = rateService.convertToNavaRate(rate);
            rateService.createRate(navaRate);
            // Set the bought rate to the shipment
            navaShipment.setRate(navaRate);
            shipmentService.modifyShipment(navaShipment);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(
                shipmentService.convertToBuyShipmentResponse(navaShipment),
                HttpStatus.OK);
    }

    @GetMapping("/rates/{shipmentId}")
    public ResponseEntity<List<com.easypost.model.Rate>> getRates(@PathVariable Long shipmentId) {
        Shipment navaShipment = shipmentService.retrieveShipment(shipmentId);
        List<com.easypost.model.Rate> rates;
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
                                                Shipment navaShipment) {
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
