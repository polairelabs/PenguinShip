package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.refreshtoken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {
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
                        "Bearer"
                )
        );
    }
}
