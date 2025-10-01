package com.montreal.oauth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Resposta completa de autenticação JWT após validação bem-sucedida do token 2FA")
public class JwtResponseDTO {

    @Schema(
        description = "Token JWT de acesso para autenticação nas APIs",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;
    
    @Schema(
        description = "Token de refresh para renovação do access token",
        example = "refresh-token-string-here"
    )
    private String token;
    
    @Schema(description = "Detalhes completos do usuário autenticado")
    private AuthResponseDTO userDetails;
    
    @Schema(
        description = "Indica se o usuário precisa fornecer token 2FA",
        example = "false"
    )
    private Boolean requiresToken;

}