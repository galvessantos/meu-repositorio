package com.montreal.oauth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteRegistrationRequest {

    @NotNull
    private Long userId;

    private String imageBase64;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirmation;

    @NotBlank
    private String token;

}
