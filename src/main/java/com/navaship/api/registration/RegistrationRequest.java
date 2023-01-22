package com.navaship.api.registration;

import com.navaship.api.validators.Password;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.*;

@Getter
@AllArgsConstructor
@ToString
public class RegistrationRequest {
    // http://emailregex.com/
    @NotNull(message = "Firstname is required")
    @NotEmpty(message = "Firstname must not be empty")
    private String firstName;
    @NotNull(message = "Lastname is required")
    @NotEmpty(message = "Lastname must not be empty")
    private String lastName;
    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email must not be empty")
    @Email(message = "Email is not valid", regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;
    @NotNull(message = "City is required")
    @NotEmpty(message = "City must not be empty")
    private String city;
    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email must not be empty")
    private String address;
    @NotNull(message = "State is required")
    @NotEmpty(message = "State must not be empty")
    private String state;
    //TODO: Add Phone number validator
    @NotNull(message = "Phone number is required")
    @NotEmpty(message = "Phone number must not be empty")
    private String phoneNumber;
    @Password
    private String password;
    @NotNull(message = "A subscription needs to be selected")
    @NotEmpty(message = "A subscription needs to be set")
    private String stripePriceId;
}
