package com.montreal.core.domain.exception;

public class UploadStorageApiException extends RuntimeException {
    public UploadStorageApiException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
