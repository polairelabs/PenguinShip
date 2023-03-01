package com.navaship.api.auth;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private AuthenticationManager authenticationManager;


    public Authentication authenticate(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException ex) {
            throw new DisabledException("Account is disabled", ex);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }
    }
}
