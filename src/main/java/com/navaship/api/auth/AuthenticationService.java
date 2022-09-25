package com.navaship.api.auth;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthenticationService {
    @Value("${navaship.app.jwtIssuer}")
    private String jwtIssuer;

    @Value("${navaship.app.jwtExpirationMs}")
    private long jwtAccessTokenExpiryMs;

    private JwtEncoder jwtEncoder;


    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtAccessTokenExpiryMs))
                .subject(subject);
        // Set claims set
        claims.forEach(claimsSetBuilder::claim); // Equivalent of a lambda function: .foreach((k, v) -> setBuilder.setClaim(k, v))

        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
