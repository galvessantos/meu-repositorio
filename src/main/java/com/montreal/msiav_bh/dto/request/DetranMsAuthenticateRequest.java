package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetranMsAuthenticateRequest(@JsonProperty("cnpj") String username, @JsonProperty("senha") String password) {
}
