package com.navaship.api.addresses;

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
    @Size(max = 95)
    private String street1;
    private String street2;
    @NotBlank
    @NotNull
    @Size(max = 35)
    private String city;
    @NotBlank
    @NotNull
    @Size(max = 35)
    @Size(max = 35)
    private String state;
    @NotBlank
    @NotNull
    @Size(max = 7)
    @NotBlank
    @NotNull
    private String zip;
    @NotBlank
    @NotNull
    @Size(max = 35)
    @NotBlank
    @NotNull
    private String country;
    private Boolean residential;
    private String name;
    private String company;
    private String phone;
    private String email;
}