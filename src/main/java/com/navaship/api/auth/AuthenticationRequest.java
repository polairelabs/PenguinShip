package com.navaship.api.auth;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class AuthenticationRequest {
    private String email;
    private String password;
}
