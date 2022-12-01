package com.navaship.api.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class BigDecimalLengthValidator implements ConstraintValidator<BigDecimalLength, BigDecimal> {
    private int max;

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || value.toString().length() <= max;
    }

    @Override
    public void initialize(BigDecimalLength constraintAnnotation) {
        this.max = constraintAnnotation.maxLength();
    }
}