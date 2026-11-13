package ceu.biolab.cmm.legacyApi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FiniteValidator.class)
public @interface Finite {
    String message() default "must be finite";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
