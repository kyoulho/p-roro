package io.playce.roro.mw.asmt.jboss.customfields;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;


public class InterFaceElConstraintValidator implements ConstraintValidator<InterFaceElPattern, String> {
    public static final String  regex = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$";
    @Override public boolean isValid(String value, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile(regex);
        return value != null &&  pattern.pattern().matches(value) ;
    }
}
