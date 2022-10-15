package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.refreshtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;


    @PostMapping("/login")
    @JsonView(AuthViews.Default.class)
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationService.authenticate(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword());
        AppUser user = (AppUser) authentication.getPrincipal();
        String accessToken = authenticationService.createAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return ResponseEntity.ok(
                new AuthenticationResponse(
                        accessToken,
                        refreshToken,
                        user,
                        TOKEN_TYPE
                )
        );
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest.getToken()).orElseThrow(
                () -> new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token cannot be processed")
        );

        if (refreshTokenService.validateExpiration(refreshToken)) {
            throw new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        refreshTokenService.delete(refreshToken);
        AppUser user = refreshToken.getUser();

        return ResponseEntity.ok(new RefreshTokenResponse(
                authenticationService.createAccessToken(user),
                refreshTokenService.createRefreshToken(user).getToken(),
                TOKEN_TYPE
        ));
    }
}
