package com.navaship.api.auth;

import com.navaship.api.validators.Password;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@ToString
public class RegistrationRequest {
    @Size(max = 254)
    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email must not be empty")
    @Email(message = "Email is not valid", regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;

    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password must not be empty")
    @Password
    private String password;

    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password must not be empty")
    private String confirmPassword;

    @Size(max = 50)
    @NotNull(message = "Firstname is required")
    @NotEmpty(message = "Firstname must not be empty")
    private String firstName;

    @Size(max = 50)
    @NotNull(message = "Lastname is required")
    @NotEmpty(message = "Lastname must not be empty")
    private String lastName;

    @Size(max = 50)
    @NotNull(message = "City is required")
    @NotEmpty(message = "City must not be empty")
    private String city;

    @Size(max = 50)
    @NotNull(message = "Address is required")
    @NotEmpty(message = "Address must not be empty")
    private String address;

    @Size(max = 50)
    @NotNull(message = "State is required")
    @NotEmpty(message = "State must not be empty")
    private String state;

    @Size(min = 7, max = 15)
    @NotNull(message = "Phone number is required")
    @NotEmpty(message = "Phone number must not be empty")
    private String phoneNumber;
}
