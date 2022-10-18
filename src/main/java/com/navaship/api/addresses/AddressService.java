package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressService {
    private AddressRepository addressRepository;


    public Address saveAddress(Address address, AppUser user) {
        address.setUser(user);
        return addressRepository.save(address);
    }

    public List<Address> getAllAddresses(AppUser user) {
        return addressRepository.findAllByUser(user);
    }

    public Optional<Address> findById(Long id) {
        return addressRepository.findById(id);
    }

    public Address modifyAddress(Address address) {
        return addressRepository.save(address);
    }

    public Address deleteAddress(Address address) {
        addressRepository.delete(address);
        return address;
    }

    public Address retrieveAddress(Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found")
        );
    }
}
