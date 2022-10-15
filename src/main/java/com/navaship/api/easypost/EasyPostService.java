package com.navaship.api.easypost;

import com.easypost.model.*;
import com.navaship.api.addresses.Addresses;
import com.navaship.api.packages.Packages;
import org.springframework.stereotype.Service;
import com.easypost.EasyPost;
import com.easypost.exception.EasyPostException;

import java.util.HashMap;
import java.util.Map;

@Service
public class EasyPostService {

    public void createShipment(EasyPostObject fromAddressMap, EasyPostObject toAddressMap, EasyPostObject parcelMap) throws EasyPostException {
        Map<String, Object> shipmentMap = new HashMap<String, Object>();
        shipmentMap.put("from_address", fromAddressMap);
        shipmentMap.put("to_address", toAddressMap);
        shipmentMap.put("parcel", parcelMap);

        Shipment shipment = Shipment.create(shipmentMap);
        shipment.buy(shipment.lowestRate());
    }

    public Address createAddress(Addresses addresses) throws EasyPostException {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", addresses.getName());
        addressMap.put("company", addresses.getCompany());
        addressMap.put("street1", addresses.getStreet1());
        addressMap.put("street2", addresses.getStreet2());
        addressMap.put("city", addresses.getCity());
        addressMap.put("state", addresses.getRegion());
        addressMap.put("country", addresses.getCountry());
        addressMap.put("zip", addresses.getPostalCode());
        addressMap.put("phone", addresses.getPostalCode());

        return Address.create(addressMap);
    }



}
