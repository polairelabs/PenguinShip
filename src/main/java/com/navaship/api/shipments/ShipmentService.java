package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import com.navaship.api.stripe.StripeService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class ShipmentService {
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;
    private StripeService stripeService;


    public Shipment createShipment(Shipment shipment,
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

    public Page<Shipment> findAllShipments(AppUser user, int pageNumber, int pageSize, String field, Sort.Direction direction) {
        return shipmentRepository.findAllByUser(user, PageRequest.of(pageNumber, pageSize).withSort(Sort.by(direction, field)));
    }

    public int retrieveUserShipmentsCount(AppUser user) {
        return shipmentRepository.countByUser(user);
    }

    public void modifyShipment(Shipment shipment) {
        shipmentRepository.save(shipment);
    }

    public void deleteShipment(Shipment shipment) {
        shipmentRepository.delete(shipment);
    }

    public Shipment retrieveShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found")
        );
    }

    public Shipment retrieveShipmentFromEasypostId(String easypostShipmentId) {
        return shipmentRepository.findShipmentByEasypostShipmentId(easypostShipmentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found")
        );
    }

    // Return this response when user creates a shipment
    public ShipmentCreatedResponse convertToShipmentCreateResponse(com.easypost.model.Shipment easypostShipment) {
        return modelMapper.map(easypostShipment, ShipmentCreatedResponse.class);
    }

    // Return this response when user buys a rate
    public BuyShipmentResponse convertToBuyShipmentResponse(Shipment shipment) {
        return modelMapper.map(shipment, BuyShipmentResponse.class);
    }

    // Return this response to return database entries of shipment
    public ShipmentResponse convertToShipmentResponse(Shipment shipment) {
        return modelMapper.map(shipment, ShipmentResponse.class);
    }
}
