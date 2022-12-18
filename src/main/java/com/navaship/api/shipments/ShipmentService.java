package com.navaship.api.shipments;

import com.easypost.model.Shipment;
import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ShipmentService {
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;


    public NavaShipment createShipment(NavaShipment shipment,
                                       AppUser user,
                                       Address fromAddress,
                                       Address toAddress,
                                       Package parcel,
                                       String additionalInfoJson) {
        shipment.setUser(user);
        shipment.setFromAddress(fromAddress);
        shipment.setToAddress(toAddress);
        shipment.setParcel(parcel);
        shipment.setAdditionalInfoJson(additionalInfoJson);
        return shipmentRepository.save(shipment);
    }

    public List<NavaShipment> findAllShipments(AppUser user) {
        return shipmentRepository.findAllByUser(user);
    }

    public void modifyShipment(NavaShipment shipment) {
        shipmentRepository.save(shipment);
    }

    public NavaShipment retrieveShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found")
        );
    }
    public NavaShipment retrieveShipment(String easypostShipmentId) {
        return shipmentRepository.findShipmentByEasypostShipmentId(easypostShipmentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found")
        );
    }

    public BuyShipmentResponse convertToBuyShipmentResponse(NavaShipment shipment) {
        return modelMapper.map(shipment, BuyShipmentResponse.class);
    }

    public ShipmentResponse convertToShipmentResponse(Shipment easypostShipment) {
        return modelMapper.map(easypostShipment, ShipmentResponse.class);
    }
}
