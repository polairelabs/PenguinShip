package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.refreshtoken.RefreshTokenRequest;
import com.navaship.api.refreshtoken.RefreshTokenService;
import com.navaship.api.registration.RegistrationRequest;
import com.navaship.api.registration.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationManager authenticationManager;

    private AuthenticationService authenticationService;
    private RegistrationService registrationService;


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        AppUser appUser = (AppUser) authentication.getPrincipal();

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);

        String accessToken = authenticationService.createAccessToken(appUser.getEmail(), claims);
        return ResponseEntity.ok(new AuthenticationResponse(accessToken, "", appUser));
    }

    @PostMapping("/register")
    public AppUser register(@RequestBody RegistrationRequest request) {
        // TODO Check for valid email
        return registrationService.register(request);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        // From old refresh token, generate new refresh token and new access token
        return null;
    }

    private Authentication authenticate(String email, String password) throws DisabledException, BadCredentialsException {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException ex) {
            throw new DisabledException("Your email address is not verified", ex);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("You have entered an invalid username or password", ex);
        }
    }
}
