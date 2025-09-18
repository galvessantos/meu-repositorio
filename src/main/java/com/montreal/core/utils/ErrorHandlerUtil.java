package com.montreal.core.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandlerUtil {

    /**
     * Método utilitário para tratar erros em controllers.
     * 
     * @param errorMessage Mensagem de erro personalizada
     * @param exception    Exceção capturada (pode ser null)
     * @return ResponseEntity com status 500 e mensagem padronizada
     */
    public static ResponseEntity<Map<String, Object>> handleError(String errorMessage, Exception exception) {
        if (exception != null) {
            log.error("Erro: {}", errorMessage, exception);
        } else {
            log.error("Erro: {}", errorMessage);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "message", errorMessage
                ));
    }

    /**
     * Cria uma exceção do tipo ResponseStatusException para Not Found (404).
     * 
     * @param message Mensagem de erro personalizada.
     * @return ResponseStatusException com código HTTP 404.
     */
    public static ResponseStatusException createNotFoundException(String message) {
        log.warn("Recurso não encontrado: {}", message);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Cria uma exceção do tipo ResponseStatusException para erro de regra de negócio (400).
     * 
     * @param message Mensagem de erro personalizada.
     * @return ResponseStatusException com código HTTP 400.
     */
    public static ResponseStatusException createBusinessException(String message) {
        log.warn("Erro de regra de negócio: {}", message);
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
