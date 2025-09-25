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
        name = "PasswordResetResponse",
        description = "Resposta da operação de redefinição de senha"
)
public class PasswordResetResponse {

    @Schema(
            description = "Mensagem informativa sobre o resultado da operação",
            example = "Senha redefinida com sucesso"
    )
    private String message;

    @Schema(
            description = "Indica se a operação foi bem-sucedida",
            example = "true"
    )
    private boolean success;

    @Schema(
            description = "Token de acesso JWT (retornado apenas quando login automático é realizado)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            nullable = true
    )
    private String accessToken;

    @Schema(
            description = "Token de refresh (retornado apenas quando login automático é realizado)",
            example = "uuid-refresh-token-123",
            nullable = true
    )
    private String refreshToken;

    @Schema(
            description = "Dados do usuário autenticado (retornado apenas quando login automático é realizado)",
            nullable = true
    )
    private LoginResponseDTO userDetails;

    @Schema(
            description = "Indica se o usuário deve validar token (2FA) após definir a senha",
            example = "true",
            nullable = true
    )
    private Boolean requiresToken;
}