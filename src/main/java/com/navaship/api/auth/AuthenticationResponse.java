package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navaship.api.appuser.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String firstName;
    private String lastName;
    private String email;
    private AppUserRole role;
}
