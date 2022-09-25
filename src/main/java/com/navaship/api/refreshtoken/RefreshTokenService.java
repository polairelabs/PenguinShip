package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.exception.TokenRefreshException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {
    @Value("${navaship.app.refreshTokenExpirationMs}")
    private long refreshTokenExpiryMs;

    private RefreshTokenRepository refreshTokenRepository;
    private AppUserRepository appUserRepository;

    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }



    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        optionalAppUser.ifPresent(refreshToken::setUser);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiryMs));
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        return refreshToken;
    }

    public RefreshToken validateExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getRefreshToken(), "Refresh token has expired");
        }
        // else return the same unexpired token
        return token;
    }
}
