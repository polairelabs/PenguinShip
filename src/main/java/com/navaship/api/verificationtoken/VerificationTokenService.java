package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.refreshtoken.RefreshToken;
import com.navaship.api.refreshtoken.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    @Value("${navaship.app.verificationTokenExpirationMs}")
    private long verificationTokenExpiryMs;

    private final AppUserRepository appUserRepository;
    private final VerificationTokenRepository verificationTokenRepository;


    public Optional<VerificationToken> findByVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public AppUser                                                                                                                                                                                                                                                                                                                                                                                                             enableUserAccount(AppUser appUser) {
        appUser.setEnabled(true);
        appUserRepository.save(appUser);
        return appUser;
    }

    public VerificationToken createVerificationToken(Long userId) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(appUserRepository.findById(userId).get());
        verificationToken.setExpiryDate(Instant.now().plusMillis(verificationTokenExpiryMs));
        return verificationToken;
    }

    public VerificationToken validateExpiration(VerificationToken verificationToken) {
        if (verificationToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new VerificationTokenException(verificationToken.getToken(), "Verification token has expired");
        }
        return verificationToken;
    }
}
