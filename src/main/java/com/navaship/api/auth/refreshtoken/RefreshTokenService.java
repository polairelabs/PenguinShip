package com.navaship.api.auth.refreshtoken;

import com.navaship.api.appuser.AppUserRepository;

import java.util.Optional;

public class RefreshTokenService {
    private Long refreshTokenDurationMs;
    private RefreshTokenRepository refreshTokenRepository;
    private AppUserRepository appUserRepository;

    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }


}
