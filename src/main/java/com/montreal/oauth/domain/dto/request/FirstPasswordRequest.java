package com.montreal.oauth.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FirstPasswordRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirmation;
}

