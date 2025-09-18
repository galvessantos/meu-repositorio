package com.montreal.oauth.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "PasswordResetValidateResponse",
        description = "Resposta da validação de token de redefinição de senha"
)
public class PasswordResetValidateResponse {

    @Schema(
            description = "Indica se o token é válido e pode ser usado",
            example = "true"
    )
    private boolean valid;

    @Schema(
            description = "Mensagem informativa sobre o status da validação",
            example = "Token is valid"
    )
    private String message;
}