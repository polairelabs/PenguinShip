package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    // https://www.bezkoder.com/spring-boot-refresh-token-jwt/

    @Value("${navaship.app.refreshTokenExpirationMs}")
    private long refreshTokenExpiryMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;


    public Optional<RefreshToken> findByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        RefreshToken refreshToken = new RefreshToken();
        optionalAppUser.ifPresent(refreshToken::setUser);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiryMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException(refreshToken.getToken(), "Refresh token has expired");
        }
        // else return the same unexpired token
        return refreshToken;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        return optionalAppUser.map(refreshTokenRepository::deleteByUser).orElse(-1);
    }
}
