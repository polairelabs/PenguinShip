package com.navaship.api.password;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordConstraintsValidator.class)
public @interface Password {
    String message() default "Password is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
