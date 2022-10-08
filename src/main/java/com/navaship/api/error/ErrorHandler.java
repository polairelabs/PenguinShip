package com.navaship.api.error;

import com.navaship.api.refreshtoken.RefreshTokenException;
import com.navaship.api.verificationtoken.VerificationTokenException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {
    // Global exception handler class which translates specific types of errors to HTTP response codes with custom messages
    // https://github.com/brunocleite/spring-boot-exception-handling

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(new ErrorMessage(HttpStatus.BAD_REQUEST, "Malformed JSON request"));
    }

    /* Custom exception handlers not overridden by ResponseEntityExceptionHandler */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponseEntity(new ErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponseEntity(new ErrorMessage(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(DisabledException.class)
    protected ResponseEntity<Object> handleDisabledException(DisabledException ex) {
        return buildResponseEntity(new ErrorMessage(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Object> handleEntityNotFound(ResponseStatusException ex) {
        return buildResponseEntity(new ErrorMessage(ex.getStatus(), ex.getReason()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    protected ResponseEntity<Object> handleRefreshTokenException(RefreshTokenException ex) {
        return buildResponseEntity(new ErrorMessage(ex.getStatusCode(), ex.getMessage()));
    }

    @ExceptionHandler(VerificationTokenException.class)
    protected ResponseEntity<Object> handleVerificationTokenException(VerificationTokenException ex) {
        return buildResponseEntity(new ErrorMessage(ex.getStatusCode(), ex.getMessage()));
    }

    private ResponseEntity<Object> buildResponseEntity(ErrorMessage errorMessage) {
        return new ResponseEntity<>(errorMessage, errorMessage.getStatus());
    }
}
