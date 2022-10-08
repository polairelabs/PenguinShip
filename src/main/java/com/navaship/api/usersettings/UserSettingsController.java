package com.navaship.api.usersettings;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.verificationtoken.VerificationTokenService;
import com.navaship.api.verificationtoken.VerificationTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/settings")
public class UserSettingsController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;


    @PostMapping("/verify-account")
    public ResponseEntity<String> createVerificationToken(JwtAuthenticationToken principal) {
        // Request new token if account not yet verified, create new token and invalid (delete) existing one
        String email = principal.getTokenAttributes().get("email").toString();
        Optional<AppUser> optionalUser = appUserService.findByEmail(email);

        return optionalUser
                .filter(user -> !user.isEnabled()).stream().findFirst()
                .map(verificationTokenService::findByUser)
                .map(verificationToken -> {
                    verificationToken.ifPresent(verificationTokenService::delete);
                    verificationTokenService.createVerificationToken(optionalUser.get(), VerificationTokenType.VERIFY_ACCOUNT);
                    String message = String.format("Account verification link has been sent to %s", email);
                    return ResponseEntity.ok(message);
                }).orElseThrow(() -> new RuntimeException("Interval server error"));
    }
}
