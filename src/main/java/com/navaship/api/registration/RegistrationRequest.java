package com.navaship.api.registration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    @NotEmpty
    private final String firstName;
    @NotEmpty
    private final String lastName;
    @NotBlank
    @Email
    @NotEmpty(message = "Email cannot be empty")
    private final String email;
    // TODO Add password validation
    private final String password;
}
