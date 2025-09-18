package com.montreal.utils;

import com.montreal.core.exception_handler.Problem;
import com.montreal.core.exception_handler.ProblemType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionUtils {
    public static Problem getMethodArgumentNotValidProblem(MethodArgumentNotValidException ex, MessageSource messageSource, HttpStatus status) {
        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";

        BindingResult bindingResult = ex.getBindingResult();

        List<Problem.Object> problemObjects = bindingResult.getAllErrors().stream()
                .map(objectError -> buildProblem(objectError, messageSource))
                .toList();

        return createProblemBuilder(status, problemType, detail)
                .objects(problemObjects)
                .userMessage("Um ou mais campos estão inválidos. Corrija os erros e tente novamente.")
                .build();
    }

    public static Problem.Object buildProblem(ObjectError objectError, MessageSource messageSource) {
        String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());

        String name = objectError.getObjectName();

        if (objectError instanceof FieldError fieldError) {
            name = fieldError.getField();
        }

        return Problem.Object.builder().name(name).userMessage(message).build();
    }

    public static Problem.ProblemBuilder createProblemBuilder(HttpStatus status, ProblemType problemType, String detail) {
        return Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(detail);
    }
}
