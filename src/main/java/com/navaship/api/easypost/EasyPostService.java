package com.navaship.api.easypost;

import com.easypost.EasyPost;
import com.easypost.exception.EasyPostException;
import com.easypost.model.Address;
import com.easypost.model.Shipment;
import com.navaship.api.packages.Package;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    public Shipment buyShipment(String easypostShipmentId, String rate) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        return shipment.buy(rate);
    }

    public Shipment insure(String easypostShipmentId, String amountInUSD) throws EasyPostException {
        EasyPost.apiKey = easyPostApiKey;
        Shipment shipment = Shipment.retrieve(easypostShipmentId);
        shipment.insure(amountInUSD);
        return shipment;
    }
}
