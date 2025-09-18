package com.montreal.core.domain.exception;

public class EmailException extends RuntimeException {

    public EmailException(String message, Throwable e) {
        super(message, e);
    }

}
