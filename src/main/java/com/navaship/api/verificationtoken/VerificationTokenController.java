package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.sendgrid.SendGridEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/account")
@RequiredArgsConstructor
public class VerificationTokenController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;
    private final SendGridEmailService sendGridEmailService;

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> sendEmailVerificationLink(@Valid @RequestBody String email) {
        // Send verification email to user
        Optional<AppUser> optionalUser = appUserService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Email not found");
        }

        AppUser user = optionalUser.get();
        if (user.isEnabled()) {
            throw new VerificationTokenException(HttpStatus.FORBIDDEN, "Something went wrong");
        }

        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.findByUser(user);
        if (optionalVerificationToken.isPresent()) {
            VerificationToken verificationToken = optionalVerificationToken.get();
            verificationTokenService.delete(verificationToken);
        }

        verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_ACCOUNT);
        try {
            sendGridEmailService.sendHTML(email, "Verify your account", "Please");
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong sending email");
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Email verification link has been sent to %s", email));

        return ResponseEntity.ok(message);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String emailVerificationToken) {
        // Check if token exists, validates if not expired, then retrieve the user and enable his account
        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.findByToken(emailVerificationToken);
        if (optionalVerificationToken.isEmpty()) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link");
        }

        VerificationToken verificationToken = optionalVerificationToken.get();
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
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody String email) {
        // Create password token and send by email, if email doesn't exist, don't send it
        Optional<AppUser> optionalUser = appUserService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Email not found");
        }

        AppUser user = optionalUser.get();
        if (user.isEnabled()) {
            throw new VerificationTokenException(HttpStatus.FORBIDDEN, "Something went wrong");
        }

        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.findByUser(user);
        if (optionalVerificationToken.isPresent()) {
            VerificationToken verificationToken = optionalVerificationToken.get();
            verificationTokenService.delete(verificationToken);
        }

        verificationTokenService.createVerificationToken(user, VerificationTokenType.RESET_PASSWORD);
        try {
            sendGridEmailService.sendHTML(email, "Password reset request", "Please");
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong sending email");
        }

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password reset link has been sent to %s", email));

        return ResponseEntity.ok(message);
    }

    @PostMapping("/confirm-password")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@RequestParam("token") String passwordResetToken, @RequestBody String password) {
        // Confirm password reset valid and reset it for user associated with that token
        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.findByToken(passwordResetToken);
        if (optionalVerificationToken.isEmpty()) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link");
        }

        VerificationToken verificationToken = optionalVerificationToken.get();
        if (verificationTokenService.validateExpiration(verificationToken)) {
            verificationTokenService.delete(verificationToken);
            throw new VerificationTokenException(HttpStatus.GONE, "Password reset link has expired");
        }

        AppUser user = verificationToken.getUser();
        appUserService.changePassword(user, password);
        verificationTokenService.delete(verificationToken);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Password was successfully reset for %s", user.getEmail()));

        return ResponseEntity.ok(message);
    }
}
