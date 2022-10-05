package com.navaship.api.verificationtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/verify-account")
public class VerificationTokenController {
    private final VerificationTokenService verificationTokenService;


    @PostMapping
    public ResponseEntity<String> createVerificationToken(JwtAuthenticationToken principal) {
        principal.getName();
        // Retrieve email from JWT
        // From USER SETTINGS, request new token if account not yet verified, create new token and invalid (delete) existing one
        Long userId = 2L;
        return verificationTokenService.findVerificationTokenByUser(userId)
                .map(VerificationToken::getUser)
                .map(user -> {
                    verificationTokenService.deleteByUser(user.getId());
                    VerificationToken verificationToken = verificationTokenService.createVerificationToken(userId);
                    String message = String.format("Account verification link has been sent to %s", verificationToken.getUser().getEmail());
                    return ResponseEntity.ok(message);
                }).orElseThrow(() -> new VerificationTokenException(HttpStatus.UNAUTHORIZED, "IDK"));
    }

    @GetMapping
    public ResponseEntity<String> verifyToken(@RequestParam("token") String token) {
        // Checks if token exists and validates its expiry date, for then to retrieve the user and enable his account
        return verificationTokenService
                .findByVerificationToken(token)
                .map(verificationTokenService::validateExpiration)
                .map(VerificationToken::getUser)
                .map(verificationTokenService::enableUserAccount)
                .map(user -> {
                    String message = String.format("Account enabled for %s", user.getEmail());
                    return ResponseEntity.ok(message);
                }).orElseThrow(() -> new VerificationTokenException(HttpStatus.BAD_REQUEST, "Invalid account verification link"));
    }
}
