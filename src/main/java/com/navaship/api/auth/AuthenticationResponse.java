package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    @JsonProperty("user")
    private AppUser appUser;
}
