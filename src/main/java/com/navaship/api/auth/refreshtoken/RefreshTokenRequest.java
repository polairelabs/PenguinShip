package com.navaship.api.auth.refreshtoken;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RefreshTokenRequest {
    private String refreshToken;
}
