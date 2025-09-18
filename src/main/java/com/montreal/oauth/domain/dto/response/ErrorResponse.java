package com.montreal.oauth.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse extends ApiResponse {
    private int errorCode;
    private String details;

    public ErrorResponse(boolean status, String message, int errorCode, String details) {
        super(status, message);
        this.errorCode = errorCode;
        this.details = details;
    }
}