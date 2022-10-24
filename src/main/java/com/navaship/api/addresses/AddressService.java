package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;


    public Address saveAddress(Address address, AppUser user) {
        address.setUser(user);
        return addressRepository.save(address);
    }

    public List<Address> findAllAddresses(AppUser user) {
        return addressRepository.findAllByUser(user);
    }

    public Address modifyAddress(Long addressId, Address address) {
        address.setId(addressId);
        return addressRepository.save(address);
    }

    public void deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
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
