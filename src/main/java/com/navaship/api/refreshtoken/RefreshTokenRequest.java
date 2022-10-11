package com.navaship.api.refreshtoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenRequest {
    private long id;
    @JsonProperty("refresh_token")
    private String token;
}
