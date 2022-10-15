package com.navaship.api.verificationtoken;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VerificationTokenException extends RuntimeException {
    private final HttpStatus statusCode;

    public VerificationTokenException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
