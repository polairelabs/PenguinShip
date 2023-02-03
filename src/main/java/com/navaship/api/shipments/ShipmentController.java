package com.navaship.api.shipments;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navaship.api.addresses.Address;
import com.navaship.api.addresses.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import com.navaship.api.person.PersonService;
import com.navaship.api.person.PersonType;
import com.navaship.api.rates.Rate;
import com.navaship.api.rates.RateService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.navaship.api.common.ListApiConstants.*;

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
    private StripeService stripeService;
    private PersonService personService;


    @GetMapping
    public ResponseEntity<ListApiResponse<ShipmentResponse>> getAllUserShipments(JwtAuthenticationToken principal,
                                                                                 @RequestParam(value = "offset", defaultValue = DEFAULT_PAGE_NUMBER + "") int offset,
                                                                                 @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                                 @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                                 @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = retrieveUserFromJwt(principal);
        ListApiResponse<ShipmentResponse> listApiResponse = new ListApiResponse<>();

        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Decrement page number to match zero-based index
        int zeroBasedPageNumber = offset - 1;

        try {
            Page<Shipment> shipmentsWithPagination = shipmentService.findAllShipments(user, zeroBasedPageNumber, pageSize, sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
            listApiResponse.setData(shipmentsWithPagination.map(shipmentService::convertToShipmentResponse).toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        int totalCount = shipmentService.retrieveUserShipmentsCount(user);
        int totalPages = (int) Math.round(totalCount / (double) pageSize);
        listApiResponse.setTotalCount(totalCount);
        listApiResponse.setTotalPages(totalPages);
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
                                                                  @Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest) {
        Address fromAddress = addressService.retrieveAddress(shipmentCreateRequest.fromAddressId);
        checkAddressBelongsToUser(principal, fromAddress);

        Address toAddress = addressService.retrieveAddress(shipmentCreateRequest.toAddressId);
        checkAddressBelongsToUser(principal, toAddress);

        Package parcel = packageService.retrievePackage(shipmentCreateRequest.parcelId);
        checkParcelBelongsToUser(principal, parcel);

        boolean isSameAddress = fromAddress.getStreet1().equals(toAddress.getStreet1())
                && fromAddress.getCity().equals(toAddress.getCity())
                && fromAddress.getState().equals(toAddress.getState())
                && fromAddress.getCountry().equals(toAddress.getCountry())
                && fromAddress.getZip().equals(toAddress.getZip());

        if (Objects.equals(fromAddress.getId(), toAddress.getId()) || isSameAddress) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and delivery address cannot be the same");
        }

        fromAddress.setName(shipmentCreateRequest.senderName);
        fromAddress.setCompany(shipmentCreateRequest.senderCompany);
        fromAddress.setPhone(shipmentCreateRequest.senderPhone);
        fromAddress.setEmail(shipmentCreateRequest.senderEmail);

        toAddress.setName(shipmentCreateRequest.receiverName);
        toAddress.setCompany(shipmentCreateRequest.receiverCompany);
        toAddress.setPhone(shipmentCreateRequest.receiverPhone);
        toAddress.setEmail(shipmentCreateRequest.receiverEmail);

        AppUser user = retrieveUserFromJwt(principal);
        com.easypost.model.Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);
            Shipment navaShipment = shipmentService.createShipment(
                    shipment.getId(),
                    ShipmentStatus.DRAFT,
                    user,
                    fromAddress,
                    toAddress,
                    parcel
            );

            if (isPersonInformationPresent(shipmentCreateRequest, PersonType.SENDER)) {
                personService.createPerson(
                        navaShipment,
                        shipmentCreateRequest.senderName,
                        shipmentCreateRequest.senderCompany,
                        shipmentCreateRequest.senderPhone,
                        shipmentCreateRequest.senderEmail,
                        PersonType.SENDER
                );
            }

            if (isPersonInformationPresent(shipmentCreateRequest, PersonType.RECEIVER)) {
                personService.createPerson(
                        navaShipment,
                        shipmentCreateRequest.receiverName,
                        shipmentCreateRequest.receiverCompany,
                        shipmentCreateRequest.receiverPhone,
                        shipmentCreateRequest.receiverEmail,
                        PersonType.RECEIVER
                );
            }
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters supplied");
        }

        // A rates array is return with ShipmentCreatedResponse object
        ShipmentCreatedResponse shipmentCreatedResponse = shipmentService.convertToShipmentCreateResponse(shipment);
        return new ResponseEntity<>(shipmentCreatedResponse, HttpStatus.OK);
    }

    @PostMapping("/buy")
    public ResponseEntity<ShipmentBoughtResponse> buyShipmentRate(JwtAuthenticationToken principal,
                                                                  @Valid @RequestBody ShipmentBuyRateRequest shipmentBuyRateRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Shipment navaShipment = shipmentService.retrieveShipmentFromEasypostId(shipmentBuyRateRequest.getEasypostShipmentId());
        checkShipmentBelongsToUser(principal, navaShipment);

        try {
            // Create payment intent
            com.easypost.model.Rate rate = com.easypost.model.Rate.retrieve(shipmentBuyRateRequest.getEasypostRateId());
            int rateInCents = Math.round(rate.getRate() * 100);

            if (user.getSubscriptionDetail() == null || user.getSubscriptionDetail().getStripeCustomerId() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User is not a customer");
            }

            String customerId = user.getSubscriptionDetail().getStripeCustomerId();
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(customerId, rateInCents, "USD");

            Map<String, Object> confirmParams = new HashMap<>();
            // confirmParams.put("return_url", "https://your-website.com/success");
            PaymentIntent confirmedPaymentIntent = paymentIntent.confirm(confirmParams);

            if (confirmedPaymentIntent.getStatus().equals("succeeded")) {
                com.easypost.model.Shipment shipment = easyPostService.buyShipmentRate(
                        shipmentBuyRateRequest.getEasypostShipmentId(),
                        shipmentBuyRateRequest.getEasypostRateId()
                );

                navaShipment.setStatus(ShipmentStatus.PURCHASED);
                rate = shipment.getSelectedRate();

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
            } else {
                System.out.println("Payment failed!!!");
            }
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, extractEasypostErrorMessage(e.getMessage()));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(
                shipmentService.convertToBuyShipmentResponse(navaShipment),
                HttpStatus.OK);
    }

    @GetMapping("/rates/{shipmentId}")
    public ResponseEntity<List<com.easypost.model.Rate>> getRates(JwtAuthenticationToken principal,
                                                                  @PathVariable Long shipmentId) {
        Shipment navaShipment = shipmentService.retrieveShipment(shipmentId);
        checkShipmentBelongsToUser(principal, navaShipment);

        List<com.easypost.model.Rate> rates;
        try {
            rates = easyPostService.getShipmentRates(navaShipment.getEasypostShipmentId());
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(rates, HttpStatus.OK);
    }

    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<Map<String, String>> deleteShipment(JwtAuthenticationToken principal,
                                                              @PathVariable Long shipmentId) {
        Shipment navaShipment = shipmentService.retrieveShipment(shipmentId);
        checkShipmentBelongsToUser(principal, navaShipment);
        if (navaShipment.getStatus() != ShipmentStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot delete a shipment with status " + navaShipment.getStatus());
        }

        shipmentService.deleteShipment(navaShipment);
        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully deleted shipment %d", shipmentId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }

    private void checkShipmentBelongsToUser(JwtAuthenticationToken principal,
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

    private String extractEasypostErrorMessage(String errorMessage) {
        String message = "Error";
        try {
            Pattern pattern = Pattern.compile("Response body: (\\{.*\\})");
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                String jsonString = matcher.group(1);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonString);
                JsonNode error = jsonNode.get("error");
                message = error.get("message").asText();
                if (message.endsWith(".")) {
                    message = message.substring(0, message.length() - 1);
                }
            }
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Easypost Error");
        }
        return message;
    }

    private boolean isPersonInformationPresent(ShipmentCreateRequest shipmentCreateRequest, PersonType type) {
        return switch (type) {
            case SENDER -> Strings.isNotBlank(shipmentCreateRequest.getSenderName()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getSenderCompany()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getSenderPhone()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getSenderEmail());
            case RECEIVER -> Strings.isNotBlank(shipmentCreateRequest.getReceiverName()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getReceiverCompany()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getReceiverPhone()) ||
                    Strings.isNotBlank(shipmentCreateRequest.getReceiverEmail());
        };
    }
}
