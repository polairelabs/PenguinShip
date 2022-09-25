package com.navaship.api.refreshtoken;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RefreshTokenRequest {
    private long id;
    private String refreshToken;
}
