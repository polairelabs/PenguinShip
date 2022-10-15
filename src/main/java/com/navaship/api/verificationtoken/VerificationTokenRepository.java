package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(AppUser user);

    Optional<VerificationToken> findByUserAndTokenType(AppUser user, VerificationTokenType tokenType);
}
