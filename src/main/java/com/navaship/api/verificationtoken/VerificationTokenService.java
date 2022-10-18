package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
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

    private final VerificationTokenRepository verificationTokenRepository;


    public Optional<VerificationToken> findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public Optional<VerificationToken> findByUserAndTokenType(AppUser user, VerificationTokenType tokenType) {
        return verificationTokenRepository.findByUserAndTokenType(user, tokenType);
    }

    public void delete(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
    }

    public VerificationToken createVerificationToken(AppUser user, VerificationTokenType tokenType) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setTokenType(tokenType);
        verificationToken.setExpiryDate(Instant.now().plusMillis(verificationTokenExpiryMs));
        return verificationTokenRepository.save(verificationToken);
    }

    public boolean validateExpiration(VerificationToken verificationToken) {
        return verificationToken.getExpiryDate().compareTo(Instant.now()) < 0;
    }
}
