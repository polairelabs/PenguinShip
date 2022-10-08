package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.refreshtoken.*;
import com.navaship.api.registration.RegistrationRequest;
import com.navaship.api.registration.RegistrationService;
import com.navaship.api.verificationtoken.VerificationTokenService;
import com.navaship.api.verificationtoken.VerificationTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public static final String TOKEN_TYPE = "Bearer";

    private final AppUserService appUserService;
    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;


    @PostMapping("/login")
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
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        TOKEN_TYPE
                )
        );
    }

    @PostMapping("/register")
    public AppUser register(@Valid @RequestBody RegistrationRequest request) {
        Optional<AppUser> optionalUser = appUserService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        AppUser user = registrationService.register(request);
        verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_ACCOUNT);
        verificationTokenService.sendVerificationEmail();

        return user;
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(refreshTokenRequest.getToken());
        if (optionalRefreshToken.isEmpty()) {
            throw new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token cannot be processed");
        }

        RefreshToken refreshToken = optionalRefreshToken.get();
        if (refreshTokenService.validateExpiration(refreshToken)) {
            throw new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        refreshTokenService.delete(refreshToken);
        AppUser user = optionalRefreshToken.get().getUser();
        return ResponseEntity.ok(new RefreshTokenResponse(
                authenticationService.createAccessToken(user),
                refreshTokenService.createRefreshToken(user).getToken(),
                TOKEN_TYPE
        ));
    }
}
