package com.navaship.api.verificationtoken;

import com.navaship.api.password.Password;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {
    @Password
    private String password;
}
