package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
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


    public Optional<VerificationToken> findByVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public Optional<VerificationToken> findVerificationTokenByUser(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        return optionalAppUser.map(verificationTokenRepository::findByUser)
                .orElseThrow(() -> new RuntimeException("Not found user"));
    }

    public int deleteByUser(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        return optionalAppUser.map(verificationTokenRepository::deleteByUser).orElse(-1);
    }

    public AppUser enableUserAccount(AppUser appUser) {
        appUser.setEnabled(true);
        appUserRepository.save(appUser);
        return appUser;
    }

    public VerificationToken createVerificationToken(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        if (optionalAppUser.isEmpty()) {
            throw new RuntimeException("User does not exist");
        }
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(optionalAppUser.get());
        verificationToken.setExpiryDate(Instant.now().plusMillis(verificationTokenExpiryMs));
        return verificationToken;
    }

    public VerificationToken validateExpiration(VerificationToken verificationToken) {
        if (verificationToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            verificationTokenRepository.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Verification token has expired");
        }
        return verificationToken;
    }

    public void sendVerificationEmail() {
        // TODO
    }
}
