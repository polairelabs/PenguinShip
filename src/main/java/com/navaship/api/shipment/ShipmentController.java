package com.navaship.api.shipment;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navaship.api.activity.ActivityLoggerService;
import com.navaship.api.activity.ActivityMessageType;
import com.navaship.api.address.Address;
import com.navaship.api.address.AddressService;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.jwt.JwtService;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageService;
import com.navaship.api.person.PersonService;
import com.navaship.api.person.PersonType;
import com.navaship.api.rate.Rate;
import com.navaship.api.rate.RateResponse;
import com.navaship.api.rate.RateService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
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
import java.math.BigDecimal;
import java.util.*;
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
    private SubscriptionDetailService subscriptionDetailService;
    private ActivityLoggerService activityLoggerService;
    private JwtService jwtService;


    @GetMapping
    public ResponseEntity<ListApiResponse<ShipmentResponse>> getAllUserShipments(JwtAuthenticationToken principal,
                                                                                 @RequestParam(value = "offset", defaultValue = DEFAULT_PAGE_NUMBER + "") int offset,
                                                                                 @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                                 @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                                 @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
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
    public ResponseEntity<ShipmentRatesResponse> createShipment(JwtAuthenticationToken principal,
                                                                @Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);

        SubscriptionDetail subscriptionDetail = user.getSubscriptionDetail();
        SubscriptionPlan subscriptionPlan = subscriptionDetail.getSubscriptionPlan();

        if (subscriptionDetail.getCurrentLimit() == subscriptionPlan.getMaxLimit()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Monthly shipment creation limit reached. Please consider deleting some unused shipments or upgrading your plan");
        }

        Address fromAddress = addressService.retrieveAddress(shipmentCreateRequest.getFromAddressId());
        jwtService.checkResourceBelongsToUser(principal, fromAddress);

        Address toAddress = addressService.retrieveAddress(shipmentCreateRequest.getToAddressId());
        jwtService.checkResourceBelongsToUser(principal, toAddress);

        Package parcel = packageService.retrievePackage(shipmentCreateRequest.getParcelId());
        jwtService.checkResourceBelongsToUser(principal, parcel);

        boolean isSameAddress = fromAddress.getStreet1().equals(toAddress.getStreet1())
                && fromAddress.getCity().equals(toAddress.getCity())
                && fromAddress.getState().equals(toAddress.getState())
                && fromAddress.getCountry().equals(toAddress.getCountry())
                && fromAddress.getZip().equals(toAddress.getZip());

        if (Objects.equals(fromAddress.getId(), toAddress.getId()) || isSameAddress) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and delivery address cannot be the same");
        }

        fromAddress.setName(shipmentCreateRequest.getSenderName());
        fromAddress.setCompany(shipmentCreateRequest.getSenderCompany());
        fromAddress.setPhone(shipmentCreateRequest.getSenderPhone());
        fromAddress.setEmail(shipmentCreateRequest.getSenderEmail());

        toAddress.setName(shipmentCreateRequest.getReceiverName());
        toAddress.setCompany(shipmentCreateRequest.getReceiverCompany());
        toAddress.setPhone(shipmentCreateRequest.getReceiverPhone());
        toAddress.setEmail(shipmentCreateRequest.getReceiverEmail());

        com.easypost.model.Shipment shipment = null;
        try {
            shipment = easyPostService.createShipment(fromAddress, toAddress, parcel);

            // Creating Shipment here
            Shipment myShipment = shipmentService.createShipment(
                    shipment.getId(),
                    ShipmentStatus.DRAFT,
                    user,
                    fromAddress,
                    toAddress,
                    parcel
            );

            if (isPersonInformationPresent(shipmentCreateRequest, PersonType.SENDER)) {
                personService.createPerson(
                        myShipment,
                        shipmentCreateRequest.getSenderName(),
                        shipmentCreateRequest.getSenderCompany(),
                        shipmentCreateRequest.getSenderPhone(),
                        shipmentCreateRequest.getSenderEmail(),
                        PersonType.SENDER
                );
            }

            if (isPersonInformationPresent(shipmentCreateRequest, PersonType.RECEIVER)) {
                personService.createPerson(
                        myShipment,
                        shipmentCreateRequest.getReceiverName(),
                        shipmentCreateRequest.getReceiverCompany(),
                        shipmentCreateRequest.getReceiverPhone(),
                        shipmentCreateRequest.getReceiverEmail(),
                        PersonType.RECEIVER
                );
            }

            // Insert activity message
            activityLoggerService.insert(user, myShipment, activityLoggerService.getShipmentCreatedMessage(myShipment), ActivityMessageType.NEW);

            // Increment currentLimit
            subscriptionDetail.setCurrentLimit(subscriptionDetail.getCurrentLimit() + 1);
            subscriptionDetailService.modifySubscriptionDetail(subscriptionDetail);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, extractEasypostErrorMessage(e.getMessage()));
        }

        ShipmentRatesResponse shipmentRatesResponse = new ShipmentRatesResponse();
        shipmentRatesResponse.setId(shipment.getId());
        shipmentRatesResponse.setRates(calculateShipmentRates(shipment.getRates(), subscriptionPlan));
        return new ResponseEntity<>(shipmentRatesResponse, HttpStatus.OK);
    }

    @PostMapping("/buy")
    public ResponseEntity<ShipmentBoughtResponse> buyShipmentRate(JwtAuthenticationToken principal,
                                                                  @Valid @RequestBody ShipmentBuyRateRequest shipmentBuyRateRequest) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        String easypostShipmentId = shipmentBuyRateRequest.getEasypostShipmentId();
        Shipment myShipment = shipmentService.retrieveShipmentFromEasypostId(easypostShipmentId);
        jwtService.checkResourceBelongsToUser(principal, myShipment);

        if (myShipment.getStatus() != ShipmentStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rates for shipment with status of " + myShipment.getStatus() + " cannot be purchased");
        }

        try {
            com.easypost.model.Rate rate = com.easypost.model.Rate.retrieve(shipmentBuyRateRequest.getEasypostRateId());

            // Calculate rate in cents
            BigDecimal currentRate = rateService.calculateRate(rate, user.getSubscriptionDetail().getSubscriptionPlan());
            int rateInCents = currentRate.multiply(new BigDecimal(100)).intValue();

            // Calculate insurance in cents
            boolean isInsured = shipmentBuyRateRequest.getIsInsured();
            BigDecimal insuranceAmount = shipmentBuyRateRequest.getInsuranceAmount();

            if (user.getSubscriptionDetail() == null || user.getSubscriptionDetail().getStripeCustomerId() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User is not a customer");
            }

            if (isInsured && insuranceAmount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid insurance amount");
            }

            if (isInsured) {
                if (insuranceAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal insuranceFee = rateService.calculateInsuranceFee(shipmentBuyRateRequest.getInsuranceAmount());
                    int insuranceFeeInCents = insuranceFee.multiply(new BigDecimal(100)).intValue();
                    rateInCents += insuranceFeeInCents;
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid insurance amount");
                }
            }

            String customerId = user.getSubscriptionDetail().getStripeCustomerId();
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(customerId, rateInCents, "USD");

            // Map<String, Object> confirmParams = new HashMap<>();
            // confirmParams.put("return_url", "https://your-website.com/success");
            PaymentIntent confirmedPaymentIntent = paymentIntent.confirm();

            if (confirmedPaymentIntent.getStatus().equals("succeeded")) {
                com.easypost.model.Shipment shipment;
                if (isInsured) {
                    shipment = easyPostService.buyShipmentRateWithInsurance(
                            shipmentBuyRateRequest.getEasypostShipmentId(),
                            shipmentBuyRateRequest.getEasypostRateId(),
                            insuranceAmount.toString()
                    );

                    myShipment.setInsured(true);
                    myShipment.setInsuranceAmount(insuranceAmount);
                } else {
                    shipment = easyPostService.buyShipmentRate(
                            shipmentBuyRateRequest.getEasypostShipmentId(),
                            shipmentBuyRateRequest.getEasypostRateId()
                    );
                }

                myShipment.setStatus(ShipmentStatus.PURCHASED);
                rate = shipment.getSelectedRate();

                // Modify shipment with new generated (from easypost) attributes trackingCode and labelUrl
                myShipment.setTrackingCode(shipment.getTrackingCode());
                if (shipment.getPostageLabel() != null) {
                    myShipment.setPostageLabelUrl(shipment.getPostageLabel().getLabelUrl());
                }

                if (shipment.getTracker() != null) {
                    myShipment.setPublicTrackingUrl(shipment.getTracker().getPublicUrl());
                }

                Rate myRate = rateService.convertToRate(rate);
                myRate.setRate(currentRate);
                rateService.createRate(myRate);
                // Set the bought rate to the user's shipment
                myShipment.setRate(myRate);

                // Update user's shipment
                shipmentService.modifyShipment(myShipment);

                // Insert activity message
                activityLoggerService.insert(user, myShipment, activityLoggerService.getShipmentBoughtMessage(myShipment), ActivityMessageType.PURCHASE);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment failed");
            }
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, extractEasypostErrorMessage(e.getMessage()));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(
                shipmentService.convertToBoughtShipmentResponse(myShipment),
                HttpStatus.OK);
    }

    @PostMapping("/refund/{shipmentId}")
    public ResponseEntity<Map<String, String>> refundShipment(JwtAuthenticationToken principal,
                                                              @PathVariable Long shipmentId) {
        Shipment myShipment = shipmentService.retrieveShipment(shipmentId);
        jwtService.checkResourceBelongsToUser(principal, myShipment);

        try {
            easyPostService.refund(myShipment.getEasypostShipmentId());
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing refund");
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully processed refund for shipment %d", shipmentId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    @GetMapping("/rates/{shipmentId}")
    public ResponseEntity<ShipmentRatesResponse> getRates(JwtAuthenticationToken principal,
                                                          @PathVariable Long shipmentId) {
        Shipment myShipment = shipmentService.retrieveShipment(shipmentId);
        jwtService.checkResourceBelongsToUser(principal, myShipment);

        SubscriptionDetail subscriptionDetail = myShipment.getUser().getSubscriptionDetail();
        SubscriptionPlan subscriptionPlan = subscriptionDetail.getSubscriptionPlan();

        List<RateResponse> shipmentRates;
        try {
            List<com.easypost.model.Rate> rates = easyPostService.getShipmentRates(myShipment.getEasypostShipmentId());
            shipmentRates = calculateShipmentRates(rates, subscriptionPlan);
        } catch (EasyPostException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        ShipmentRatesResponse shipmentRatesResponse = new ShipmentRatesResponse();
        shipmentRatesResponse.setId(myShipment.getEasypostShipmentId());
        shipmentRatesResponse.setRates(shipmentRates);
        return new ResponseEntity<>(shipmentRatesResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<Map<String, String>> deleteShipment(JwtAuthenticationToken principal,
                                                              @PathVariable Long shipmentId) {
        Shipment myShipment = shipmentService.retrieveShipment(shipmentId);
        jwtService.checkResourceBelongsToUser(principal, myShipment);
        if (myShipment.getStatus() != ShipmentStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot delete a shipment with status " + myShipment.getStatus());
        }

        shipmentService.deleteShipment(myShipment);

        // If deleted (it's because it was a draft shipment), subtract 1 from current limit of the current month
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        int currentLimit = user.getSubscriptionDetail().getCurrentLimit();
        user.getSubscriptionDetail().setCurrentLimit(currentLimit - 1);
        appUserService.modifyUser(user);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully deleted shipment %d", shipmentId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    private List<RateResponse> calculateShipmentRates(List<com.easypost.model.Rate> rates, SubscriptionPlan subscriptionPlan) {
        List<RateResponse> shipmentRates = new ArrayList<>();
        for (com.easypost.model.Rate rate : rates) {
            Rate myRate = rateService.convertToRate(rate);
            BigDecimal finalRate = rateService.calculateRate(rate, subscriptionPlan);
            myRate.setRate(finalRate);
            shipmentRates.add(rateService.convertToRateResponse(myRate));
        }
        // Sort by lowest rate first
        shipmentRates.sort(Comparator.comparing(r -> new BigDecimal(r.getRate())));
        return shipmentRates;
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
