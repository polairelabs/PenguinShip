package com.navaship.api.addresses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private Boolean residential;
    private String name;
    private String company;
    private String phone;
    private String email;

    public AddressResponse(Address address) {
        id = address.getId();
        street1 = address.getStreet1();
        street2 = address.getStreet2();
        city = address.getCity();
        state = address.getState();
        zip = address.getZip();
        country = address.getCountry();
        residential = address.getResidential();
        name = address.getName();
        company = address.getCompany();
        phone = address.getPhone();
        email = address.getEmail();
    }
}
