package com.navaship.api.easypost;

import com.easypost.EasyPost;
import com.easypost.exception.EasyPostException;
import com.easypost.model.Rate;
import com.easypost.model.Shipment;
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

    @Value("${navaship.app.easypost.apikey}")
    private String easyPostApiKey;

    public Shipment createShipment(com.navaship.api.addresses.Address fromAddress,
                               com.navaship.api.addresses.Address toAddress,
                               com.navaship.api.packages.Package parcel) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Map<String, Object> shipmentMap = new HashMap<String, Object>();
        shipmentMap.put(FROM_ADDRESS_MAP_KEY, fromAddress.toAddressMap());
        shipmentMap.put(TO_ADDRESS_MAP_KEY, toAddress.toAddressMap());
        shipmentMap.put(PARCEL_MAP_KEY, parcel.toPackageMap());

        return Shipment.create(shipmentMap);
    }

    public Map<String, Object> buyShipmentRate(String easypostShipmentId, String easypostRateId) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;

        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        Rate rate = Rate.retrieve(easypostRateId);

        // Map<String, Object> buyMap = new HashMap<String, Object>();
        // buyMap.put("rate", easypostRateId);
        // buyMap.put("insurance", 249.99);
        //  rate = shipment.lowestRate();

        // The boughtShipment will contain the tracking_code and postage_label attributes
        Shipment boughtShipment = shipment.buy(rate);

        Map<String, Object> results = new HashMap<>();
        results.put("boughtShipment", boughtShipment);
        results.put("boughtRate", rate); // The rate the shipment was bought

        return results;
    }

    public List<Rate> getShipmentRates(String easypostShipmentId) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        return shipment.getRates();
    }

    public Shipment insure(String easypostShipmentId, String amountInUSD) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        shipment.insure(amountInUSD);
        return shipment;
    }

    public Shipment refund(String easypostShipmentId, Rate rate) throws EasyPostException {
        // TODO finish this or delete
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        shipment.refund();
        return shipment;
    }
}
