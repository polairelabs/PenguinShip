package com.navaship.api.addresses;

import lombok.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AddressService {

    private AddressRepository addressRepository;

    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

}
