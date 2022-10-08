package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    // https://www.bezkoder.com/spring-boot-refresh-token-jwt/

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;
    @Value("${navaship.app.refreshTokenExpirationMs}")
    private long refreshTokenExpiryMs;


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
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiryMs));
        return refreshTokenRepository.save(refreshToken);
    }

    public boolean validateExpiration(RefreshToken refreshToken) {
        return refreshToken.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        return optionalAppUser.map(refreshTokenRepository::deleteByUser).orElse(-1);
    }
}
