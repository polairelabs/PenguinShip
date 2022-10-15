package com.navaship.api.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Used for field validation errors
 */
@Getter
@Setter
@AllArgsConstructor
public class FieldValidationError {
    private String field;
    private String message;
}
