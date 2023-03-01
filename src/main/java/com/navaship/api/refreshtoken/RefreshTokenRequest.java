package com.navaship.api.refreshtoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class RefreshTokenRequest {
    @NotNull
    @JsonProperty("refresh_token")
    private String token;
}
