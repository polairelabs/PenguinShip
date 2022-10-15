package com.navaship.api.error;

import com.navaship.api.refreshtoken.RefreshTokenException;
import com.navaship.api.sendgrid.SendGridEmailException;
import com.navaship.api.verificationtoken.VerificationTokenException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {
    // Global exception handler class which translates specific types of errors to HTTP response codes with custom messages
    // https://github.com/brunocleite/spring-boot-exception-handling

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(new ErrorDetails(HttpStatus.BAD_REQUEST, "Malformed JSON request", ex));
    }

    /* Custom exception handlers not overridden by ResponseEntityExceptionHandler */
    @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponseEntity(new ErrorDetails(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponseEntity(new ErrorDetails(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DisabledException.class)
    protected ResponseEntity<Object> handleDisabledException(DisabledException ex) {
        return buildResponseEntity(new ErrorDetails(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Object> handleEntityNotFound(ResponseStatusException ex) {
        return buildResponseEntity(new ErrorDetails(ex.getStatus(), ex.getReason()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RefreshTokenException.class)
    protected ResponseEntity<Object> handleRefreshTokenException(RefreshTokenException ex) {
        return buildResponseEntity(new ErrorDetails(ex.getStatusCode(), ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(VerificationTokenException.class)
    protected ResponseEntity<Object> handleVerificationTokenException(VerificationTokenException ex) {
        return buildResponseEntity(new ErrorDetails(ex.getStatusCode(), ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(SendGridEmailException.class)
    protected ResponseEntity<Object> handleVerificationTokenException(SendGridEmailException ex) {
        return buildResponseEntity(new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<FieldValidationError> validationErrors = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )).toList();
        return buildResponseEntity(new ErrorDetails(HttpStatus.BAD_REQUEST, "Improper data submitted", validationErrors));
    }

    private ResponseEntity<Object> buildResponseEntity(ErrorDetails errorDetails) {
        return new ResponseEntity<>(errorDetails, errorDetails.getStatus());
    }
}
