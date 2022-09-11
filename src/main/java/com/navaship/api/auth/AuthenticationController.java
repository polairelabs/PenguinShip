package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RestController
@RequestMapping(path = "api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationManager authenticationManager;
    private JwtEncoder jwtEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Instant now = Instant.now();
        long expiry = 36000L;

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("navaship.api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(format("%s %s", appUser.getEmail(), appUser.getPassword()))
                .claim("scope", scope)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return ResponseEntity.ok(token);
    }

    private Authentication authenticate(String email, String password) throws DisabledException, BadCredentialsException {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }
}
