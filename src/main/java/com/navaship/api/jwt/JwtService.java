package com.navaship.api.jwt;

import com.navaship.api.address.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.packages.Package;
import com.navaship.api.shipment.Shipment;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final AppUserService appUserService;

    private final JwtEncoder jwtEncoder;

    @Value("${navaship.api.domain}")
    private String jwtIssuer;
    @Value("${navaship.api.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;
    @Value("${navaship.api.emailVerificationExpirationMs}")
    private long verificationTokenExpirationMs;
    @Value("${jwt.public.key}")
    private RSAPublicKey rsaPublicKey;

    public String createJwtAccessToken(Authentication authentication, AppUser user) {
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessTokenExpirationMs))
                .claim("id", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("scope", scope)
                .subject(authentication.getName());

        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    // Used in confirm email and reset pass
    public String createJwtTokenForValidation(AppUser user, String token) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(verificationTokenExpirationMs))
                .claim("email", user.getEmail())
                .claim("token", token)
                .subject(user.getId().toString());

        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public boolean verifyToken(String token) {
        try {
            // Parse and verify the JWT token
            return SignedJWT.parse(token).verify(new RSASSAVerifier(rsaPublicKey));
        } catch (Exception e) {
            return false;
        }
    }

    public Object getClaim(String token, String name) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            return signedJwt.getJWTClaimsSet().getClaim(name);
        } catch (Exception e) {
            return null;
        }
    }

    public AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        UUID userId = UUID.fromString((String) principal.getTokenAttributes().get("id"));
        return appUserService.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
    }

    public void checkResourceBelongsToUser(JwtAuthenticationToken principal, Address address) {
        UUID userId = UUID.fromString((String) principal.getTokenAttributes().get("id"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }

    public void checkResourceBelongsToUser(JwtAuthenticationToken principal, Package parcel) {
        UUID userId = UUID.fromString((String) principal.getTokenAttributes().get("id"));
        if (!parcel.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }

    public void checkResourceBelongsToUser(JwtAuthenticationToken principal, Shipment shipment) {
        UUID userId = UUID.fromString((String) principal.getTokenAttributes().get("id"));
        if (!shipment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }
}
