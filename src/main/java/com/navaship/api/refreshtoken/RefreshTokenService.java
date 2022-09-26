package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.exception.RefreshTokenException;
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
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        RefreshToken refreshToken = new RefreshToken();
        optionalAppUser.ifPresent(refreshToken::setUser);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiryMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(), "Refresh token has expired");
        }
        // else return the same unexpired token
        return token;
    }

    public int deleteByUserId(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        return optionalAppUser.map(appUser -> refreshTokenRepository.deleteByUser(appUser)).orElse(-1);
    }
}
