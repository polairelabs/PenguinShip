package com.navaship.api.auth;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class JwtUtils {

    public static final String JWT_ISSUER = "navaship.com";
    public static final long JWT_ACCESS_TOKEN_EXPIRY = 3600L;
    public static final long JWT_REFRESH_TOKEN_EXPIRY = 7200L;

    private JwtEncoder jwtEncoder;

//    private String extractExpiresAt(String token) {
//        // return expires at claim
//    }

    private String createToken(String subject, Consumer<Map<String, Object>> claims) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(JWT_ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(JWT_ACCESS_TOKEN_EXPIRY))
                .subject(subject)
                .claims(claims)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    private String createRefreshToken(String subject) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(JWT_ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(JWT_REFRESH_TOKEN_EXPIRY))
                .subject(subject)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
