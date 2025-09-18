package com.montreal.oauth.exception;

import com.montreal.core.exception_handler.Problem;
import com.montreal.oauth.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.montreal.utils.ExceptionUtils.getMethodArgumentNotValidProblem;

/**
 * Manipulador global de exceções apenas para o pacote com.montreal.oauth
 */
@RestControllerAdvice(basePackages = "com.montreal.oauth")
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    /**
     * Captura exceções quando um recurso não é encontrado (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Recurso não encontrado");
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

    /**
     * Captura erros de validação de campos nos DTOs (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Problem problem = getMethodArgumentNotValidProblem(ex, messageSource, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(problem, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura erros inesperados dentro do pacote `com.montreal.oauth` e retorna status 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erro interno do servidor");
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }
}
