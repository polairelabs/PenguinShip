package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.exception.RefreshTokenException;
import com.navaship.api.refreshtoken.RefreshToken;
import com.navaship.api.refreshtoken.RefreshTokenRequest;
import com.navaship.api.refreshtoken.RefreshTokenResponse;
import com.navaship.api.refreshtoken.RefreshTokenService;
import com.navaship.api.registration.RegistrationRequest;
import com.navaship.api.registration.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {
    public static final String BEARER_TOKEN_TYPE = "Bearer";

    private AuthenticationManager authenticationManager;

    private AuthenticationService authenticationService;
    private RegistrationService registrationService;
    private RefreshTokenService refreshTokenService;


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationService.authenticate(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword());
        AppUser appUser = (AppUser) authentication.getPrincipal();

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);

        String accessToken = authenticationService.createAccessToken(appUser.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(appUser.getId()).getToken();
        return ResponseEntity.ok(
                new AuthenticationResponse(
                        accessToken,
                        refreshToken,
                        appUser.getFirstName(),
                        appUser.getLastName(),
                        appUser.getEmail(),
                        appUser.getRole()
                )
        );
    }

    @PostMapping("/register")
    public AppUser register(@RequestBody RegistrationRequest request) {
        // TODO Check for valid email
        return registrationService.register(request);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        return refreshTokenService
                .findByRefreshToken(refreshTokenRequest.getToken())
                .map(refreshTokenService::validateExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("scope", user.getRole());
                    String accessToken = authenticationService.createAccessToken(user.getEmail(), claims);
                    // Refresh token rotation / Should update/change refresh token
                    String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
                    return ResponseEntity.ok(new RefreshTokenResponse(
                            accessToken,
                            refreshToken,
                            BEARER_TOKEN_TYPE
                    ));
                }).orElseThrow(() -> new RefreshTokenException(refreshTokenRequest.getToken(), "Not found"));
    }
}
