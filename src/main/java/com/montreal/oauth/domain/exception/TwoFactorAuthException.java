package com.montreal.oauth.domain.exception;

public class TwoFactorAuthException extends RuntimeException {

    public TwoFactorAuthException(String message) {
        super(message);
    }

}
