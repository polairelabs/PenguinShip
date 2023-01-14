package com.navaship.api.shipments;

import com.easypost.model.Shipment;
import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Page<NavaShipment> findAllShipmentsPagination(AppUser user, int offset, int pageSize) {
        return shipmentRepository.findAllByUser(user, PageRequest.of(offset, pageSize));
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

    // Return this response when user creates a shipment
    public ShipmentCreatedResponse convertToShipmentCreateResponse(Shipment easypostShipment) {
        return modelMapper.map(easypostShipment, ShipmentCreatedResponse.class);
    }

    // Return this response when user buys a rate
    public BuyShipmentResponse convertToBuyShipmentResponse(NavaShipment shipment) {
        return modelMapper.map(shipment, BuyShipmentResponse.class);
    }

    // Return this response to return database entries of shipment
    public ShipmentResponse convertToShipmentResponse(NavaShipment shipment) {
        return modelMapper.map(shipment, ShipmentResponse.class);
    }
}
