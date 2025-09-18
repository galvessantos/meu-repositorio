package com.montreal.utils;

import com.montreal.core.exception_handler.Problem;
import com.montreal.core.exception_handler.ProblemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionUtilsTest {

    @Mock
    private MessageSource messageSource;

    @Test
    @DisplayName("Should create Problem from MethodArgumentNotValidException with field errors")
    void shouldCreateProblemFromMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("object", "fieldName", "Field error");
        ObjectError objectError = new ObjectError("objectName", "Object error");
        Locale currentLocale = LocaleContextHolder.getLocale();

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError, objectError));
        when(messageSource.getMessage(fieldError, currentLocale)).thenReturn("Field error message");
        when(messageSource.getMessage(objectError, currentLocale)).thenReturn("Object error message");

        Problem problem = ExceptionUtils.getMethodArgumentNotValidProblem(exception, messageSource, HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getType()).isEqualTo(ProblemType.DADOS_INVALIDOS.getUri());
        assertThat(problem.getTitle()).isEqualTo(ProblemType.DADOS_INVALIDOS.getTitle());
        assertThat(problem.getDetail()).isEqualTo("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.");
        assertThat(problem.getUserMessage()).isEqualTo("Um ou mais campos estão inválidos. Corrija os erros e tente novamente.");

        assertThat(problem.getObjects()).hasSize(2);
        assertThat(problem.getObjects().get(0).getName()).isEqualTo("fieldName");
        assertThat(problem.getObjects().get(0).getUserMessage()).isEqualTo("Field error message");
        assertThat(problem.getObjects().get(1).getName()).isEqualTo("objectName");
        assertThat(problem.getObjects().get(1).getUserMessage()).isEqualTo("Object error message");
    }

    @Test
    @DisplayName("Should build Problem.Object from a FieldError")
    void shouldBuildProblemObjectFromFieldError() {
        FieldError fieldError = new FieldError("object", "fieldName", "Error message");
        Locale currentLocale = LocaleContextHolder.getLocale();
        when(messageSource.getMessage(fieldError, currentLocale)).thenReturn("Translated field error");

        Problem.Object problemObject = ExceptionUtils.buildProblem(fieldError, messageSource);

        assertThat(problemObject).isNotNull();
        assertThat(problemObject.getName()).isEqualTo("fieldName");
        assertThat(problemObject.getUserMessage()).isEqualTo("Translated field error");
    }

    @Test
    @DisplayName("Should build Problem.Object from a regular ObjectError")
    void shouldBuildProblemObjectFromObjectError() {
        ObjectError objectError = new ObjectError("objectName", "Error message");
        Locale currentLocale = LocaleContextHolder.getLocale();
        when(messageSource.getMessage(objectError, currentLocale)).thenReturn("Translated object error");

        Problem.Object problemObject = ExceptionUtils.buildProblem(objectError, messageSource);

        assertThat(problemObject).isNotNull();
        assertThat(problemObject.getName()).isEqualTo("objectName");
        assertThat(problemObject.getUserMessage()).isEqualTo("Translated object error");
    }

    @Test
    @DisplayName("Should create ProblemBuilder with correct fields set")
    void shouldCreateProblemBuilderWithCorrectFields() {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Detailed error message";

        Problem problem = ExceptionUtils.createProblemBuilder(status, problemType, detail).build();

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(status.value());
        assertThat(problem.getType()).isEqualTo(problemType.getUri());
        assertThat(problem.getTitle()).isEqualTo(problemType.getTitle());
        assertThat(problem.getDetail()).isEqualTo(detail);
        assertThat(problem.getTimestamp()).isNotNull();
        assertThat(problem.getTimestamp()).isInstanceOf(OffsetDateTime.class);
    }
}