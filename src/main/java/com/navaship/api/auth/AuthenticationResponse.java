package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    @JsonView(AuthViews.Default.class)
    @JsonProperty("access_token")
    private String accessToken;
    @JsonView(AuthViews.Default.class)
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonView(AuthViews.Default.class)
    private AppUser user;
    @JsonView(AuthViews.Default.class)
    @JsonProperty("token_type")
    private String tokenType;
}
