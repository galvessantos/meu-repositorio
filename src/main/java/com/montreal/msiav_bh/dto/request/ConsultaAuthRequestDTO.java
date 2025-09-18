package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsultaAuthRequestDTO(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {}