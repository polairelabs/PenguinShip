package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    // https://www.bezkoder.com/spring-boot-refresh-token-jwt/
    @Value("${navaship.api.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public RefreshToken createRefreshToken(AppUser user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    public boolean validateExpiration(RefreshToken refreshToken) {
        return refreshToken.getExpiryDate().compareTo(Instant.now()) < 0;
    }
}
