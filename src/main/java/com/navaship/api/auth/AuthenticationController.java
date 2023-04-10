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
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.navaship.api.util.ProfileHelper;
import com.navaship.api.verificationtoken.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
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
import java.util.HashMap;
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
    @JsonView(AuthViews.Default.class)
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (appUserService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (!Objects.equals(registrationRequest.getPassword(), registrationRequest.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password provided do not match");
        }

        AppUser newUser = new AppUser(
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

        AppUser user = appUserService.createUser(newUser);

        try {
            sendVerifyEmailLink(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email to verify account. Please try again at a later time");
        }

        RegistrationResponse registrationResponse = new RegistrationResponse();
        registrationResponse.setUser(user);

        try {
            // Register user as Stripe customer
            Customer customer = stripeService.createCustomer(user);
            SubscriptionDetail subscriptionDetail = new SubscriptionDetail();
            subscriptionDetail.setStripeCustomerId(customer.getId());
            subscriptionDetail.setUser(user);
            subscriptionDetailService.createSubscriptionDetail(subscriptionDetail);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(registrationResponse, HttpStatus.CREATED);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(HttpServletRequest request) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        Cookie[] cookies = request.getCookies();
        String refreshTokenStr = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REQUEST_TOKEN_COOKIE_KEY.equals(cookie.getName())) {
                    refreshTokenStr = cookie.getValue();
                    break;
                }
            }
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr).orElseThrow(
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

        if (user.getIsEmailVerified()) {
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
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification token");
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
        appUserService.verifyUserEmail(user);
        verificationTokenService.delete(verificationToken);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> sendPasswordResetLink(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        // Send password reset link to user
        AppUser user = appUserService.findByEmail(passwordResetRequest.getEmail()).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Email not found")
        );

        verificationTokenService.findByUserAndTokenType(user, VerificationTokenType.RESET_PASSWORD)
                .ifPresent(verificationTokenService::delete);

        try {
            sendPasswordResetLink(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email to verify account. Please try again at a later time");
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password reset link has been sent to %s", passwordResetRequest.getEmail()));

        return ResponseEntity.ok(message);
    }

    @GetMapping("/password-reset/{passwordResetJwt}")
    public ResponseEntity<?> changePassword(@PathVariable String passwordResetJwt, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        if (!jwtService.verifyToken(passwordResetJwt)) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid password reset link");
        }

        String verificationTokenString = (String) jwtService.getClaim(passwordResetJwt, "token");
        if (verificationTokenString == null) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid password reset token");
        }

        VerificationToken verificationToken = verificationTokenService.findByToken(verificationTokenString).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid password reset link")
        );

        boolean isTokenExpired = verificationTokenService.validateExpiration(verificationToken);

        if (isTokenExpired) {
            verificationTokenService.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.GONE, "Password reset link has expired");
        }

        AppUser user = verificationToken.getUser();
        appUserService.changePassword(user, changePasswordRequest.getPassword());
        verificationTokenService.delete(verificationToken);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password was successfully reset for %s", user.getEmail()));

        return ResponseEntity.ok(message);
    }

    private void sendVerifyEmailLink(AppUser user) throws IOException {
        // Delete token if it exists for the user
        verificationTokenService.findByUserAndTokenType(user, VerificationTokenType.VERIFY_EMAIL).ifPresent(verificationTokenService::delete);
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_EMAIL);
        String emailVerificationJwt = jwtService.createJwtTokenForValidation(user, verificationToken.getToken());
        sendGridEmailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), emailVerificationJwt);
    }

    private void sendPasswordResetLink(AppUser user) throws IOException {
        // Delete token if it exists for the user
        verificationTokenService.findByUserAndTokenType(user, VerificationTokenType.RESET_PASSWORD).ifPresent(verificationTokenService::delete);
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.RESET_PASSWORD);
        String passwordResetJwt = jwtService.createJwtTokenForValidation(user, verificationToken.getToken());
        sendGridEmailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), passwordResetJwt);
    }
}
