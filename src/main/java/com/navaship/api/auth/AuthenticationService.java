package com.navaship.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Value("${navaship.app.jwtIssuer}")
    private String jwtIssuer;
    @Value("${navaship.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;


    public Authentication authenticate(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException ex) {
            throw new DisabledException("Your email address is not verified", ex);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("You have entered invalid credentials", ex);
        }
    }

    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtExpirationMs))
                .subject(subject);
        // Set claims set
        claims.forEach(claimsSetBuilder::claim); // Equivalent of a lambda function: .foreach((k, v) -> setBuilder.setClaim(k, v))

        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
