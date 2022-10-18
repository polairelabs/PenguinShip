package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.sendgrid.SendGridEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/account")
public class VerificationTokenController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;
    private final SendGridEmailService sendGridEmailService;

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> sendEmailVerificationLink(@Valid @RequestBody EmailConfirmationRequest emailConfirmationRequest) {
        // Send email verification link to user
        String email = emailConfirmationRequest.getEmail();
        AppUser user = retrieveUser(email);
        deleteVerificationTokenIfPresent(user, VerificationTokenType.VERIFY_EMAIL);

        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_EMAIL);
        sendGridEmailService.sendVerifyAccountEmail(email, verificationToken.getToken());

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Email verification link has been sent to %s", email));

        return ResponseEntity.ok(message);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmEmailVerified(@RequestParam("token") String emailVerificationToken) {
        // Check if token exists, validates if not expired, then retrieve the user and enable his account
        VerificationToken verificationToken = verificationTokenService.findByToken(emailVerificationToken).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link")
        );

        if (verificationTokenService.validateExpiration(verificationToken)) {
            verificationTokenService.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.GONE, "Email verification link has expired");
        }

        AppUser user = verificationToken.getUser();
        appUserService.enableUserAccount(user);
        verificationTokenService.delete(verificationToken);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Account enabled for %s", user.getEmail()));

        return ResponseEntity.ok(message);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody EmailConfirmationRequest emailConfirmationRequest) {
        // Send password reset link to user
        String email = emailConfirmationRequest.getEmail();
        AppUser user = retrieveUser(email);
        deleteVerificationTokenIfPresent(user, VerificationTokenType.RESET_PASSWORD);

        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.RESET_PASSWORD);
        sendGridEmailService.sendPasswordResetEmail(email, user.getFirstName(), verificationToken.getToken());

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password reset link has been sent to %s", email));

        return ResponseEntity.ok(message);
    }

    @PostMapping("/confirm-password")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@RequestParam("token") String passwordResetToken, @Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        // Check if token exists, validates if not expired, then retrieve the user and change his password
        VerificationToken verificationToken = verificationTokenService.findByToken(passwordResetToken).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid password reset link")
        );

        if (verificationTokenService.validateExpiration(verificationToken)) {
            verificationTokenService.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.GONE, "Password reset link has expired");
        }

        AppUser user = verificationToken.getUser();
        appUserService.changePassword(user, passwordResetRequest.getPassword());
        verificationTokenService.delete(verificationToken);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password was successfully reset for %s", user.getEmail()));

        return ResponseEntity.ok(message);
    }

    private AppUser retrieveUser(String email) {
        AppUser user = appUserService.findByEmail(email).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Email not found")
        );

        if (!user.isEnabled()) {
            throw new VerificationTokenException(HttpStatus.FORBIDDEN, "Something went wrong");
        }
        return user;
    }

    private void deleteVerificationTokenIfPresent(AppUser user, VerificationTokenType tokenType) {
        verificationTokenService.findByUserAndTokenType(user, tokenType).ifPresent(verificationTokenService::delete);
    }
}
