package com.montreal.msiav_bh.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsultaAuthResponseDTO(
        @JsonProperty("success") boolean success,
        @JsonProperty("data") TokenData data,
        @JsonProperty("message") String message
) {
    public record TokenData(
            @JsonProperty("token") String token,
            @JsonProperty("expires_in") String expiresIn
    ) {}
}

