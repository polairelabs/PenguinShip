package com.navaship.api.registration;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.sendgrid.SendGridEmailService;
import com.navaship.api.verificationtoken.VerificationToken;
import com.navaship.api.verificationtoken.VerificationTokenService;
import com.navaship.api.verificationtoken.VerificationTokenType;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1")
public class RegistrationController {
    private AppUserService appUserService;
    private VerificationTokenService verificationTokenService;
    private SendGridEmailService sendGridEmailService;


    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (appUserService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser newUser = new AppUser(
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                AppUserRole.USER
        );

        // Newly created user
        AppUser user = appUserService.createUser(newUser);
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_EMAIL);
        sendGridEmailService.sendVerifyAccountEmail(user.getEmail(), verificationToken.getToken());

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
