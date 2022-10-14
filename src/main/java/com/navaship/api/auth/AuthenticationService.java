package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
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
import java.util.HashMap;
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
            throw new DisabledException("Account is disabled", ex);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }
    }

    public String createAccessToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtExpirationMs))
                .subject(user.getEmail());
        // Set claims here! Equivalent of a lambda function: .foreach((k, v) -> setBuilder.setClaim(k, v))
        getClaimsMap(user).forEach(claimsSetBuilder::claim);
        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    private Map<String, Object> getClaimsMap(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        return claims;
    }
}
