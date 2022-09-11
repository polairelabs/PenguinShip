package com.navaship.api.auth;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class AuthenticationRequest {
    private final String email;
    private final String password;
}
