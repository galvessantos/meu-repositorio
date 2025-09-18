package com.montreal.core.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class ImageNotAllowedException extends RuntimeException {

    public ImageNotAllowedException(String message) {
        super(message);
    }
}
