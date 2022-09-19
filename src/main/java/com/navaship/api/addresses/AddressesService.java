package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Packages;
import lombok.*;
import org.springframework.stereotype.Service;

import javax.persistence.*;

@Service
@AllArgsConstructor
public class AddressesService {

    private AddressesRepository addressRepository;

    public Addresses saveAddress(Addresses address) {
        return addressRepository.save(address);
    }

}
