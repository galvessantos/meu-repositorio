package com.montreal.core.domain.exception;

public class PdfConverterException extends RuntimeException {
    public PdfConverterException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
