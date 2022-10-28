package com.navaship.api.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldValidationError {
    private String field;
    private String message;
}
