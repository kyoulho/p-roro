package io.playce.roro.mw.asmt.jboss.customfields;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InterFaceElConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InterFaceElPattern {
    String message() default "Invalid interface Pattern";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
