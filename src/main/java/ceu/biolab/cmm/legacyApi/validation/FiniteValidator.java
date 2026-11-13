package ceu.biolab.cmm.legacyApi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class FiniteValidator implements ConstraintValidator<Finite, Double> {
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value == null || Double.isFinite(value);
    }
}
