package com.navaship.api.verificationtoken;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class VerificationTokenException extends RuntimeException {
    public VerificationTokenException(String token, String message) {
        super(String.format("Failed for [%s]: [%s]", token, message));
    }
}
