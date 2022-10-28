package com.navaship.api.password;

import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class PasswordConstraintsValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        PasswordValidator passwordValidator = new PasswordValidator(
                Arrays.asList(
                        new LengthRule(8, 128),
                        new CharacterRule(EnglishCharacterData.UpperCase, 1),
                        new CharacterRule(EnglishCharacterData.Digit, 1),
                        new CharacterRule(EnglishCharacterData.Special, 1),
                        new WhitespaceRule()
                )
        );

        RuleResult result = passwordValidator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }

        constraintValidatorContext.buildConstraintViolationWithTemplate(passwordValidator.getMessages(result).stream().findFirst().get())
                .addConstraintViolation()
                .disableDefaultConstraintViolation();

        return false;
    }
}
