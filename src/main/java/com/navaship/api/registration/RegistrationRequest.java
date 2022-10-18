package com.navaship.api.registration;

import com.navaship.api.password.Password;
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
    @Min(3)
    private String firstName;
    @NotNull(message = "Lastname is required")
    @NotEmpty(message = "Lastname must not be empty")
    @Min(3)
    private String lastName;
    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email must not be empty")
    @Email(message = "Email is not valid", regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;
    @Password
    private String password;
}
