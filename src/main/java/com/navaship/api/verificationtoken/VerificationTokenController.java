package com.navaship.api.verificationtoken;

import com.navaship.api.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/verify-account")
public class VerificationTokenController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;


    @PostMapping
    public ResponseEntity<?> createVerificationToken() {
        // From u
        return null;
    }


    @GetMapping
    public ResponseEntity<String> verifyToken(@RequestParam("token") String token) {
        // Find token and validate if token has not expired, for then to retrieve the user and enable his account
        return verificationTokenService
                .findByVerificationToken(token)
                .map(verificationTokenService::validateExpiration)
                .map(VerificationToken::getUser)
                .map(verificationTokenService::enableUserAccount)
                .map(user -> {
                    String message = String.format("Account confirmed for %s", user.getEmail());
                    return ResponseEntity.ok(message);
                }).orElseThrow(() -> new VerificationTokenException(token, "Invalid account confirmation link"));
    }
}
