package com.navaship.api.easypost;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.navaship.api.activity.ActivityLoggerService;
import com.navaship.api.activity.ActivityMessageType;
import com.navaship.api.shipment.Shipment;
import com.navaship.api.shipment.ShipmentService;
import com.navaship.api.shipment.ShipmentStatus;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.webhook.WebhookService;
import com.navaship.api.webhook.WebhookType;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class EasypostWebhookProcessingService {
    private WebhookService webhookService;
    private EasypostService easyPostService;
    private ShipmentService shipmentService;
    private ActivityLoggerService activityLoggerService;
    private StripeService stripeService;


    @Async
    public void processWebhookEvent(byte[] payload, HttpServletRequest request) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        Event event = null;
        try {
            String easypostWebhookSecret = webhookService.retrieveWebhookWithType(WebhookType.EASYPOST).getSecret();
            event = easyPostService.validateWebhook(payload, headers, easypostWebhookSecret);
        } catch (EasyPostException e) {
            throw new EasyPostException(e.getMessage());
        }

        // Convert payload to Hashmap (webhookData)
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> webhookData = new HashMap<>();

        String eventJson = null;
        try {
            eventJson = objectMapper.writeValueAsString(event);
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
            webhookData = objectMapper.readValue(eventJson, mapType);
        } catch (JsonProcessingException e) {
            throw new Exception(e.getMessage());
        }

        // Extract shipment data in result
        if (webhookData != null && webhookData.containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) webhookData.get("result");
            String statusDetail = (String) result.get("statusDetail");
            if (statusDetail.equals("status_update") || statusDetail.equals("out_for_delivery") || statusDetail.equals("arrived_at_destination")) {
                String easypostShipmentId = (String) result.get("shipmentId");

                Shipment userShipment = shipmentService.retrieveShipmentFromEasypostId(easypostShipmentId);

                if (statusDetail.equals("arrived_at_destination")) {
                    userShipment.setEasypostStatus(EasypostShipmentStatus.DELIVERED);
                    userShipment.setStatus(ShipmentStatus.DELIVERED);
                    userShipment.setDeliveryDate(Instant.now());
                } else {
                    String shipmentStatus = (String) result.get("status");
                    userShipment.setEasypostStatus(EasypostShipmentStatus.valueOf(shipmentStatus.toUpperCase()));
                    Long estDeliveryDateMillis = (Long) result.get("estDeliveryDate");
                    userShipment.setDeliveryDate(Instant.ofEpochMilli(estDeliveryDateMillis));
                }

                shipmentService.updateShipment(userShipment);
                activityLoggerService.insert(userShipment.getUser(), userShipment, activityLoggerService.getShipmentStatusChangeMessage(userShipment), ActivityMessageType.STATUS_UPDATE);
            } else if (statusDetail.equals("return_update")) {
                String easypostShipmentId = (String) result.get("shipmentId");
                Shipment userShipment = shipmentService.retrieveShipmentFromEasypostId(easypostShipmentId);
                String shipmentStatus = (String) result.get("status");

                String chargeId = userShipment.getStripeChargeId();
                try {
                    // Refund successful
                    Refund refund = stripeService.refund(chargeId);
                    userShipment.setStripeRefundId(refund.getId());
                    userShipment.setStatus(ShipmentStatus.REFUND_PROCESSED);
                    userShipment.setEasypostStatus(EasypostShipmentStatus.valueOf(shipmentStatus.toUpperCase()));

                    shipmentService.updateShipment(userShipment);
                    double refundAmount = stripeService.getRefundAmount(refund);
                    activityLoggerService.insert(userShipment.getUser(), userShipment, activityLoggerService.getShipmentReturnProcessed(userShipment, refundAmount), ActivityMessageType.RETURN_PROCESSED);
                } catch (StripeException e) {
                    System.err.println("Error issuing refund: " + e.getMessage());
                }
            }
        }
    }
}
