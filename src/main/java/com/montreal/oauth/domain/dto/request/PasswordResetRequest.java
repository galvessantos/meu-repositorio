package com.montreal.oauth.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "PasswordResetRequest",
        description = "Dados para redefinir a senha usando um token válido",
        example = """
        {
          "token": "uuid-123-456-789",
          "newPassword": "Nova@123",
          "confirmPassword": "Nova@123"
        }
        """
)
public class PasswordResetRequest {

    @Schema(
            description = "Token de redefinição de senha recebido por e-mail",
            example = "uuid-123-456-789",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Token é obrigatório")
    private String token;

    @Schema(
            description = """
            Nova senha que deve atender aos seguintes critérios:
            - Tamanho: mínimo 4, máximo 8 caracteres
            - Composição: pelo menos 1 letra maiúscula e 1 minúscula
            - Caracteres especiais: pelo menos 1 dos seguintes: _ @ #
            - Números: pelo menos 1 dígito
            """,
            example = "Nova@123",
            minLength = 4,
            maxLength = 8,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 4, max = 8, message = "A senha deve ter entre 4 e 8 caracteres")
    private String newPassword;

    @Schema(
            description = "Confirmação da nova senha (deve ser idêntica à nova senha)",
            example = "Nova@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;
}