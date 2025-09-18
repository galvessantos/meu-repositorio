package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.exception_handler.ProblemType;

import lombok.Getter;

@Getter
public class InvalidParameterException extends ResponseStatusException {

    private final ProblemType problemType;

    public InvalidParameterException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.problemType = ProblemType.PARAMETRO_INVALIDO;
    }

    public InvalidParameterException(ProblemType problemType, String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.problemType = problemType;
    }

    public InvalidParameterException(ProblemType problemType, String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
        this.problemType = problemType;
    }

    public InvalidParameterException(HttpStatus status, ProblemType problemType, String message) {
        super(status, message);
        this.problemType = problemType;
    }

    public InvalidParameterException(HttpStatus status, ProblemType problemType, String message, Throwable cause) {
        super(status, message, cause);
        this.problemType = problemType;
    }
}
