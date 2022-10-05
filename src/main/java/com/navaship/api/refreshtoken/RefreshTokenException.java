package com.navaship.api.refreshtoken;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class RefreshTokenException extends RuntimeException {
    private HttpStatus statusCode;

    public RefreshTokenException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
