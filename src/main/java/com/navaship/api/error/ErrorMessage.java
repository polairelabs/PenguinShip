package com.navaship.api.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorMessage {
    private final HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    @JsonProperty("debug_message")
    private String debugMessage;
    @JsonProperty("sub_errors")
    private List<ApiValidationError> subErrors;

    public ErrorMessage(HttpStatus status) {
        timestamp = LocalDateTime.now();
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public ErrorMessage(HttpStatus status, String message) {
        timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public ErrorMessage(HttpStatus status, String message, Throwable ex) {
        timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }
}
