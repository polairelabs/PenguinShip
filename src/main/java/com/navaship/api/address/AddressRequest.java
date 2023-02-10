package com.navaship.api.address;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
public class AddressRequest {
    @NotBlank
    @NotNull
    @Size(max = 100)
    private String street1;
    @Size(max = 100)
    private String street2;
    @NotBlank
    @NotNull
    @Size(max = 40)
    private String city;
    @NotBlank
    @NotNull
    @Size(max = 40)
    @Size(max = 40)
    private String state;
    @NotBlank
    @NotNull
    @Size(max = 8)
    @NotBlank
    @NotNull
    private String zip;
    @NotBlank
    @NotNull
    @Size(max = 40)
    @NotBlank
    @NotNull
    private String country;
    @NotBlank
    @NotNull
    private String residential;
}