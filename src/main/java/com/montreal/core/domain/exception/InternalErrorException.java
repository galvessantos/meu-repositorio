package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.exception_handler.ProblemType;
import lombok.Getter;

@Getter
public class InternalErrorException extends ResponseStatusException {

    private final ProblemType problemType;

    public InternalErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        this.problemType = ProblemType.ERRO_DE_SISTEMA;
    }

    public InternalErrorException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
        this.problemType = ProblemType.ERRO_DE_SISTEMA;
    }

    public InternalErrorException(HttpStatus status, ProblemType problemType, String message, Throwable cause) {
        super(status, message, cause);
        this.problemType = problemType;
    }
}
