package com.navaship.api.addresses;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/addresses")
@AllArgsConstructor
public class AddressesController {
    private AddressesService addressService;

    @PostMapping
    public ResponseEntity<Addresses> addAddress(@RequestBody Addresses addresses) {
        return new ResponseEntity<>(addressService.saveAddress(addresses), HttpStatus.CREATED);
    }
}
