package com.navaship.api.shipments;

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
                                       Package parcel) {
        shipment.setUser(user);
        shipment.setFromAddress(fromAddress);
        shipment.setToAddress(toAddress);
        shipment.setParcel(parcel);
        return shipmentRepository.save(shipment);
    }

    public List<NavaShipment> findAllShipments(AppUser user) {
        return shipmentRepository.findAllByUser(user);
    }

    public void modifyShipment(NavaShipment shipment) {
        shipmentRepository.save(shipment);
    }

    public NavaShipmentResponse convertToNavaShipmentResponse(NavaShipment shipment) {
        return modelMapper.map(shipment, NavaShipmentResponse.class);
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

}
