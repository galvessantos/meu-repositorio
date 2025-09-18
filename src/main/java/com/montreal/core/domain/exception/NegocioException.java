package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.exception_handler.ProblemType;

import lombok.Getter;

@Getter
public class NegocioException extends ResponseStatusException {

    private final ProblemType problemType;

    // Construtor com mensagem padrão e status BAD_REQUEST
    public NegocioException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
    }

    // Construtor com ProblemType e mensagem, usando BAD_REQUEST como padrão
    public NegocioException(ProblemType problemType, String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.problemType = problemType;
    }

    // Construtor com ProblemType, mensagem e causa, usando BAD_REQUEST como padrão
    public NegocioException(ProblemType problemType, String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
        this.problemType = problemType;
    }

    // Construtor com status personalizado, ProblemType e mensagem
    public NegocioException(HttpStatus status, ProblemType problemType, String message) {
        super(status, message);
        this.problemType = problemType;
    }

    // Construtor com status personalizado, ProblemType, mensagem e causa
    public NegocioException(HttpStatus status, ProblemType problemType, String message, Throwable cause) {
        super(status, message, cause);
        this.problemType = problemType;
    }
}
