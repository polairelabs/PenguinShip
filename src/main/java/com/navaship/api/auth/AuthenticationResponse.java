package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
}
