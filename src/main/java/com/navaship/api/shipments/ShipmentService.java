package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ShipmentService {
    private ShipmentRepository shipmentRepository;


    public Shipment saveShipment(Shipment shipment,
                                 AppUser user,
                                 Address fromAddress,
                                 Address toAddress,
                                 Package parcel) {
        shipment.setUser(user);
        shipment.setFromAddress(fromAddress);
        shipment.setFromAddress(toAddress);
        shipment.setParcel(parcel);
        return shipmentRepository.save(shipment);
    }

    public List<Shipment> getAllShipments(AppUser user) {
        return shipmentRepository.findAllByUser(user);
    }
}
