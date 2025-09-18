package com.montreal.core.domain.exception;

public class ConflictUserException extends RuntimeException {

    public ConflictUserException(String message) {
        super(message);
    }
}
