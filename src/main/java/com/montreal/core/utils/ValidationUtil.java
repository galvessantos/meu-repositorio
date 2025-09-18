package com.montreal.core.utils;

import java.util.Set;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.exception_handler.ProblemType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ValidationUtil {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("\n");
            }

            throw new InvalidParameterException(ProblemType.PARAMETRO_INVALIDO, "Erro de validação: \n" + sb.toString());
        }
    }
}
