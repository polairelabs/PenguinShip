package com.navaship.api.addresses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Long id;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private Boolean residential;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
