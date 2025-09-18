package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.exception_handler.ProblemType;

import lombok.Getter;

@Getter
public class ConflictException extends ResponseStatusException {

    private final ProblemType problemType;

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
        this.problemType = ProblemType.ENTIDADE_EM_USO;
    }

    public ConflictException(ProblemType problemType, String message) {
        super(HttpStatus.CONFLICT, message);
        this.problemType = problemType;
    }

    public ConflictException(ProblemType problemType, String message, Throwable cause) {
        super(HttpStatus.CONFLICT, message, cause);
        this.problemType = problemType;
    }

    public ConflictException(HttpStatus status, ProblemType problemType, String message) {
        super(status, message);
        this.problemType = problemType;
    }

    public ConflictException(HttpStatus status, ProblemType problemType, String message, Throwable cause) {
        super(status, message, cause);
        this.problemType = problemType;
    }
}
