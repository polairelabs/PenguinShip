package com.navaship.api.verificationtoken;

import com.navaship.api.validators.Password;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password must not be empty")
    @Password
    private String password;

    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password must not be empty")
    private String confirmPassword;

    @NotNull(message = "Token is required")
    @NotEmpty(message = "Token must not be empty")
    private String token;
}
