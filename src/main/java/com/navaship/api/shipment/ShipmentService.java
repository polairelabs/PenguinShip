package com.navaship.api.shipment;

import com.navaship.api.address.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.easypost.EasypostShipmentStatus;
import com.navaship.api.packages.Package;
import com.navaship.api.shipmentaddress.ShipmentAddressType;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ShipmentService {
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;


    public Shipment createShipment(String easypostShipmentId,
                                   ShipmentStatus status,
                                   AppUser user,
                                   Address fromAddress,
                                   Address toAddress,
                                   Package parcel) {
        Shipment shipment = new Shipment();
        shipment.setEasypostShipmentId(easypostShipmentId);
        shipment.setStatus(status);
        shipment.setUser(user);
        shipment.setSourceAddress(fromAddress);
        shipment.setDeliveryAddress(toAddress);
        shipment.setParcel(parcel);

        Shipment latestShipment = getLatestShipment(user);
        if (latestShipment != null) {
            shipment.setShipmentNumber(latestShipment.getShipmentNumber() + 1);
        } else {
            shipment.setShipmentNumber(1);
        }

        return shipmentRepository.save(shipment);
    }

    public Page<Shipment> findAllShipments(AppUser user, int pageNumber, int pageSize, String field, Sort.Direction direction) {
        return shipmentRepository.findAllByUser(user, PageRequest.of(pageNumber, pageSize).withSort(Sort.by(direction, field)));
    }

    public int retrieveUserShipmentsCount(AppUser user) {
        return shipmentRepository.countByUser(user);
    }

    public int retrieveUserShipmentsCountByStatus(AppUser user, ShipmentStatus status) {
        return shipmentRepository.countByUserAndStatus(user, status);
    }

    public int retrieveUserShipmentsCountByEasypostStatus(AppUser user, EasypostShipmentStatus status) {
        return shipmentRepository.countByUserAndEasypostStatus(user, status);
    }

    public Shipment getLatestShipment(AppUser user) {
        return shipmentRepository.findTopByUserOrderByCreatedAtDesc(user);
    }

    public void updateShipment(Shipment shipment) {
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

    public BigDecimal getTotalMoneySaved(AppUser user) {
        BigDecimal totalBoughtShipmentsRate = shipmentRepository.totalSumRateByUser(user);
        BigDecimal totalRetailRate = shipmentRepository.totalSumRetailRate(user);
        if (totalBoughtShipmentsRate == null || totalRetailRate == null) {
            return BigDecimal.ZERO;
        }
        return totalRetailRate.subtract(totalBoughtShipmentsRate);
    }

    // Return this response when user buys a rate
    public ShipmentBoughtResponse convertToBoughtShipmentResponse(Shipment shipment) {
        ShipmentBoughtResponse response = modelMapper.map(shipment, ShipmentBoughtResponse.class);
        response.setFromAddress(
                shipment.getAddresses()
                        .stream()
                        .filter(shipmentAddress -> shipmentAddress.getType().equals(ShipmentAddressType.SOURCE))
                        .findFirst()
                        .get()
        );
        response.setToAddress(
                shipment.getAddresses()
                        .stream()
                        .filter(shipmentAddress -> shipmentAddress.getType().equals(ShipmentAddressType.DESTINATION))
                        .findFirst()
                        .get()
        );
        return response;
    }

    // Return this response to return database entries of shipment
    public ShipmentResponse convertToShipmentResponse(Shipment shipment) {
        return modelMapper.map(shipment, ShipmentResponse.class);
    }
}
