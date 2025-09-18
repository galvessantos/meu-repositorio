package com.montreal.oauth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckPasswordResetDTO {

    @NotBlank
    public String email;

    @NotBlank
    public String password;

    @NotBlank
    public String link;

}
