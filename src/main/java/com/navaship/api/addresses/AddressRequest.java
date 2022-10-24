package com.navaship.api.addresses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Street1 must not be empty")
    @NotNull(message = "Street1 is required")
    @Size(max = 95, message = "Street1 must be less than 95 characters")
    @Size(max = 95)
    private String street1;
    private String street2;
    @NotBlank(message = "City must not be empty")
    @NotNull(message = "City is required")
    @Size(max = 35, message = "City must be less than 35 characters")
    private String city;
    @NotBlank(message = "State must not be empty")
    @NotNull(message = "State is required")
    @Size(max = 35, message = "State must be less than 35 characters")
    @Size(max = 35)
    private String state;
    @NotBlank
    @NotNull
    @Size(max = 5, message = "Zip must be less than 5 characters")
    @NotBlank(message = "Zip must not be empty")
    @NotNull(message = "Zip is required")
    private String zip;
    @NotBlank
    @NotNull
    @Size(max = 35, message = "Country must be less than 35 characters")
    @NotBlank(message = "Country must not be empty")
    @NotNull(message = "Country is required")
    private String country;
    private Boolean residential;
    private String name;
    private String company;
    private String phone;
    private String email;
}