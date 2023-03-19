package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    @JsonView(AuthViews.Default.class)
    @JsonProperty("access_token")
    private String accessToken;
    @JsonView(AuthViews.Default.class)
    private AppUser user;
}
