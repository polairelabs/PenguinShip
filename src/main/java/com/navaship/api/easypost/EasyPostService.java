package com.navaship.api.easypost;

import com.easypost.EasyPost;
import com.easypost.exception.EasyPostException;
import com.easypost.model.Event;
import com.easypost.model.Rate;
import com.easypost.model.Shipment;
import com.easypost.model.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EasyPostService {
    public static final String FROM_ADDRESS_MAP_KEY = "from_address";
    public static final String TO_ADDRESS_MAP_KEY = "to_address";
    public static final String PARCEL_MAP_KEY = "parcel";

    @Value("${easypost.apikey}")
    private String easyPostApiKey;
    @Value("${easypost.webhook.endpoint.secret}")
    private String webhookSecret;
    @Value("${easypost.webhook.endpoint.url}")
    private String webhookUrl;


    public Shipment createShipment(com.navaship.api.address.Address fromAddress, com.navaship.api.address.Address toAddress, com.navaship.api.packages.Package parcel) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Map<String, Object> shipmentMap = new HashMap<String, Object>();
        shipmentMap.put(FROM_ADDRESS_MAP_KEY, fromAddress.toAddressMap());
        shipmentMap.put(TO_ADDRESS_MAP_KEY, toAddress.toAddressMap());
        shipmentMap.put(PARCEL_MAP_KEY, parcel.toPackageMap());

        return Shipment.create(shipmentMap);
    }

    public Shipment buyShipmentRate(String easypostShipmentId, String easypostRateId) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        Rate rate = Rate.retrieve(easypostRateId);
        return shipment.buy(rate);
    }

    public Shipment buyShipmentRateWithInsurance(String easypostShipmentId, String easypostRateId, String insuranceAmount) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        Rate rate = Rate.retrieve(easypostRateId);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("rate", rate);
        params.put("insurance", insuranceAmount);
        return shipment.buy(params);
    }

    public List<Rate> getShipmentRates(String easypostShipmentId) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        return shipment.getRates();
    }

    public Shipment refund(String easypostShipmentId) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        shipment.refund();
        return shipment;
    }

    public Webhook createWebhook() throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("url", webhookUrl);
        paramMap.put("webhook_secret", webhookSecret);
        return Webhook.create(paramMap);
    }

    public Event validateWebhook(byte[] eventBody, Map<String, Object> headers) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        return Webhook.validateWebhook(eventBody, headers, webhookSecret);
    }
}
