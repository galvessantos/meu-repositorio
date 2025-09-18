package com.montreal.core.domain.exception;

import com.montreal.core.exception_handler.ProblemType;
import lombok.Getter;

@Getter
public class CheckImageException extends RuntimeException {

    private final ProblemType problemType;

    public CheckImageException(String message) {
        super(message);
        this.problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
    }
    
}
