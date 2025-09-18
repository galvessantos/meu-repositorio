package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.exception_handler.ProblemType;

import lombok.Getter;

@Getter
public class NotFoundException extends ResponseStatusException {

    private final ProblemType problemType;

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
        this.problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
    }

    public NotFoundException(ProblemType problemType, String message) {
        super(HttpStatus.NOT_FOUND, message);
        this.problemType = problemType;
    }

    public NotFoundException(ProblemType problemType, String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
        this.problemType = problemType;
    }

    public NotFoundException(HttpStatus status, ProblemType problemType, String message) {
        super(status, message);
        this.problemType = problemType;
    }

    public NotFoundException(HttpStatus status, ProblemType problemType, String message, Throwable cause) {
        super(status, message, cause);
        this.problemType = problemType;
    }
}
