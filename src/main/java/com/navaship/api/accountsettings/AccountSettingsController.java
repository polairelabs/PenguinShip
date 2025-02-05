package com.navaship.api.accountsettings;

import com.navaship.api.appuser.AppUserService;
import com.navaship.api.verificationtoken.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/account/settings")
public class AccountSettingsController {
    private final VerificationTokenService verificationTokenService;
    private final AppUserService appUserService;


    @PostMapping("/verify-account")
    public ResponseEntity<String> createVerificationToken(JwtAuthenticationToken principal) {
        // Request new token if account not yet verified, create new token and invalid (delete) existing one
        String email = principal.getTokenAttributes().get("email").toString();
        // AppUser user = appUserService.findByEmail(email);
        return null;
    }
}
