package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.sendgrid.SendGridEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    @Value("${navaship.app.verificationTokenExpirationMs}")
    private long verificationTokenExpiryMs;
    @Value("${navaship.app.senderEmail}")
    private String senderEmail;

    private final AppUserRepository appUserRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final SendGridEmailService sendGridEmailService;


    public Optional<VerificationToken> findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public Optional<VerificationToken> findByUser(AppUser user) {
        return verificationTokenRepository.findByUser(user);
    }

    public void delete(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
    }

    public void enableUserAccount(AppUser user) {
        user.setEnabled(true);
        appUserRepository.save(user);
    }

    public VerificationToken createVerificationToken(AppUser user, VerificationTokenType tokenType) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setVerificationTokenType(tokenType);
        verificationToken.setExpiryDate(Instant.now().plusMillis(verificationTokenExpiryMs));
        return verificationTokenRepository.save(verificationToken);
    }

    public boolean validateExpiration(VerificationToken verificationToken) {
        return verificationToken.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    public void sendVerificationEmail() {
        // sendGridEmailService.sendHTML();
    }
}
