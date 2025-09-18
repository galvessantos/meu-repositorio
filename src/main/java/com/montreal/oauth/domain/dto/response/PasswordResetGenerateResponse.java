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
        name = "PasswordResetGenerateResponse",
        description = "Resposta da solicitação de redefinição de senha"
)
public class PasswordResetGenerateResponse {

    @Schema(
            description = "Mensagem informativa sobre o resultado da operação",
            example = "Password reset token generated successfully"
    )
    private String message;

    @Schema(
            description = "Link para redefinição de senha (null se login inválido)",
            example = "https://localhost:5173/reset-password?token=uuid-123",
            nullable = true
    )
    private String resetLink;
}