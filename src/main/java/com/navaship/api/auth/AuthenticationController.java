package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRoleEnum;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.jwt.JwtService;
import com.navaship.api.refreshtoken.RefreshToken;
import com.navaship.api.refreshtoken.RefreshTokenException;
import com.navaship.api.refreshtoken.RefreshTokenResponse;
import com.navaship.api.refreshtoken.RefreshTokenService;
import com.navaship.api.sendgrid.SendGridEmailService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.navaship.api.util.ProfileHelper;
import com.navaship.api.verificationtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {
    private static final String REQUEST_TOKEN_COOKIE_KEY = "refresh_token";

    private final AppUserService appUserService;
    private final StripeService stripeService;
    private final SubscriptionDetailService subscriptionDetailService;
    private final VerificationTokenService verificationTokenService;
    private final SendGridEmailService sendGridEmailService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final ProfileHelper profileHelper;

    @Value("${navaship.api.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;


    @GetMapping("/user-information")
    @JsonView(AuthViews.Default.class)
    public ResponseEntity<AppUser> retrieveUserInformation(JwtAuthenticationToken principal) {
        return new ResponseEntity<>(jwtService.retrieveUserFromJwt(principal), HttpStatus.OK);
    }

    @PostMapping("/login")
    @JsonView(AuthViews.Default.class)
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        Authentication authentication = authenticationService.authenticate(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword());
        AppUser user = (AppUser) authentication.getPrincipal();
        String accessToken = jwtService.createJwtAccessToken(authentication, user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        boolean isProdProfile = profileHelper.isProdProfileActive();

        // Create the server side cookie with HttpOnly set to true which contains the refresh token
        ResponseCookie cookie = ResponseCookie.from(REQUEST_TOKEN_COOKIE_KEY, refreshToken)
                .maxAge(refreshTokenExpirationMs / 1000)
                .httpOnly(true)
                .sameSite(isProdProfile ? "None" : "Lax")
                .secure(isProdProfile)
                .path("/api/v1/auth/refresh-token")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AuthenticationResponse(accessToken, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTemporaryUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (appUserService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (!Objects.equals(registrationRequest.getPassword(), registrationRequest.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided passwords do not match");
        }

        // Create temporary (unverified email) user
        AppUser temporaryUser = new AppUser(
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getPhoneNumber(),
                registrationRequest.getCity(),
                registrationRequest.getState(),
                registrationRequest.getAddress(),
                AppUserRoleEnum.NEW_USER
        );

        AppUser user = appUserService.createUser(temporaryUser);
        try {
            sendVerifyEmailLink(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email to verify account. Please try again at a later time");
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(HttpServletRequest request) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        Enumeration<String> pop = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();
        String refreshaToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REQUEST_TOKEN_COOKIE_KEY.equals(cookie.getName())) {
                    refreshaToken = cookie.getValue();
                    System.out.println("Got cookie refresh_token: " + refreshaToken);
                    break;
                }
            }
        }

        RefreshToken refreshToken = refreshTokenService.findByToken("refreshTokenValue").orElseThrow(
                () -> new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token cannot be processed")
        );

        if (refreshTokenService.validateExpiration(refreshToken)) {
            throw new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        AppUser user = refreshToken.getUser();
        refreshTokenService.delete(refreshToken);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new RefreshTokenResponse(
                jwtService.createJwtAccessToken(authentication, user),
                refreshTokenService.createRefreshToken(user).getToken(),
                "Bearer"
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> sendEmailVerificationLink(@RequestBody @Size(max = 254) String email) {
        AppUser user = appUserService.findByEmail(email).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Email not found")
        );

        if (user.isEnabled()) {
            throw new VerificationTokenException(HttpStatus.FORBIDDEN, "Something went wrong");
        }

        try {
            sendVerifyEmailLink(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email to verify account. Please try again at a later time");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email/{emailVerificationJwt}")
    public ResponseEntity<?> verifyEmail(@PathVariable String emailVerificationJwt) {
        if (!jwtService.verifyToken(emailVerificationJwt)) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link");
        }

        String verificationTokenString = (String) jwtService.getClaim(emailVerificationJwt, "token");
        if (verificationTokenString == null) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        VerificationToken verificationToken = verificationTokenService.findByToken(verificationTokenString).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link")
        );

        boolean isTokenExpired = verificationTokenService.validateExpiration(verificationToken);

        if (isTokenExpired) {
            verificationTokenService.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.GONE, "Email verification link has expired");
        }

        AppUser user = verificationToken.getUser();
        appUserService.enableUserAccount(user);
        verificationTokenService.delete(verificationToken);

        return ResponseEntity.ok().build();
    }

    private void sendVerifyEmailLink(AppUser user) throws IOException {
        // Delete token if it exists for the user
        verificationTokenService.findByUserAndTokenType(user, VerificationTokenType.VERIFY_EMAIL).ifPresent(verificationTokenService::delete);
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_EMAIL);
        String emailVerificationJwt = jwtService.createJwtEmailVerification(user, verificationToken.getToken());
        sendGridEmailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), emailVerificationJwt);
    }
}
