package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/account")
@RequiredArgsConstructor
public class VerificationTokenController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;

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
        verificationTokenService.sendVerificationEmail();

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Email verification link has been sent to %s", email));

        return ResponseEntity.ok(response);
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
        verificationTokenService.enableUserAccount(user);
        verificationTokenService.delete(verificationToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Account enabled for %s", user.getEmail()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestBody String email) {
        // Create password token and send by email, if email doesn't exist, don't send it
        return null;
    }

    @PostMapping("/confirm-password")
    public ResponseEntity<String> confirmPasswordReset(@RequestParam("token") String passwordResetToken, @RequestBody String password) {
        // Confirm password reset valid and reset it for user associated with that token
        // TODO create custom query to search for type
        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.findByToken(passwordResetToken);
        if (optionalVerificationToken.isEmpty()) {
            throw new VerificationTokenException(HttpStatus.UNAUTHORIZED, "Invalid email verification link");
        }
        return null;
    }
}
