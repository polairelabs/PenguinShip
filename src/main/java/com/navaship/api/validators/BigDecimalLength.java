package com.navaship.api.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { BigDecimalLengthValidator.class})
public @interface BigDecimalLength {
    int maxLength();
    String message() default "Length must be less or equal to {maxLength}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}