package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipments.NavaShipment;
import com.navaship.api.shipments.ShipmentRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressService {
    private AddressRepository addressRepository;
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;


    public Address createAddress(Address address, AppUser user) {
        address.setUser(user);
        return addressRepository.save(address);
    }

    public List<Address> findAllAddresses(AppUser user) {
        return addressRepository.findAllByUser(user);
    }

    public Address modifyAddress(Address address) {
        return addressRepository.save(address);
    }

    public void deleteAddress(Address address) {
        // Set address to null to all shipments that used that address
        for (NavaShipment shipment : address.getFromAddressShipments()) {
            shipment.setFromAddress(null);
            shipmentRepository.save(shipment);
        }
        for (NavaShipment shipment : address.getToAddressShipments()) {
            shipment.setToAddress(null);
            shipmentRepository.save(shipment);
        }
        addressRepository.delete(address);
    }

    public Address retrieveAddress(Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found")
        );
    }

    public Address convertToAddress(AddressRequest addressRequest) {
        return modelMapper.map(addressRequest, Address.class);
    }

    public AddressResponse convertToAddressResponse(Address address) {
        return modelMapper.map(address, AddressResponse.class);
    }
}
