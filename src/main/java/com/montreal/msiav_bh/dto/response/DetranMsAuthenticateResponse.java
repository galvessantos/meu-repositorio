package com.montreal.msiav_bh.dto.response;

import lombok.Builder;

@Builder
public record DetranMsAuthenticateResponse(String token, String errorMessage) {
}
