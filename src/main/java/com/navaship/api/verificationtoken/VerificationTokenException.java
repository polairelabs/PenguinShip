package com.navaship.api.verificationtoken;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class VerificationTokenException extends RuntimeException {
    private HttpStatus statusCode;

    public VerificationTokenException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
