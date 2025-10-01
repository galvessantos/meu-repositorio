package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.AuthResponseDTO;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.RefreshTokenService;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.domain.service.UserTokenService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação 2FA", description = "Endpoints para geração e validação de tokens de autenticação de dois fatores (2FA)")
public class AuthTokenController {

    private final UserService userService;
    private final UserTokenService userTokenService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
        summary = "Gerar token 2FA",
        description = "Gera um token de autenticação de dois fatores (2FA) para um usuário específico. " +
                     "O token tem validade limitada e deve ser usado para validação posterior."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token gerado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenerateTokenResponse.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "token": "A1B2C",
                          "expiresAt": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Usuário não encontrado",
                    value = """
                        {
                          "error": "Usuário não encontrado"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                        {
                          "error": "Erro interno do servidor"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/generate-token")
    public ResponseEntity<GenerateTokenResponse> generateToken(
        @Parameter(description = "Dados para geração do token 2FA", required = true)
        @RequestBody GenerateTokenRequest req) {
        
        if (req.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            UserInfo user = userService.findById(req.getUserId());
            var token = userTokenService.generateAndPersist(user);
            return ResponseEntity.ok(new GenerateTokenResponse(token.getToken(), token.getExpiresAt().toString()));
        } catch (Exception e) {
            log.error("Error generating token for user {}", req.getUserId(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Validar token 2FA",
        description = "Valida um token de autenticação de dois fatores (2FA) e, se válido, " +
                     "retorna os tokens JWT de acesso e refresh para autenticação completa do usuário."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token validado com sucesso - retorna JWT completo",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = JwtResponseDTO.class),
                examples = @ExampleObject(
                    name = "Validação bem-sucedida",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "token": "refresh-token-string",
                          "userDetails": {
                            "user": {
                              "id": 1,
                              "username": "usuario@exemplo.com",
                              "email": "usuario@exemplo.com",
                              "roles": ["USER"],
                              "cpf": "12345678901",
                              "phone": "+5511999999999",
                              "companyId": "123",
                              "isEnabled": true
                            },
                            "permissions": [],
                            "functionalities": []
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido ou expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidateTokenError.class),
                examples = {
                    @ExampleObject(
                        name = "Token inválido",
                        value = """
                            {
                              "error": "TOKEN_INVALIDO"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Token expirado",
                        value = """
                            {
                              "error": "TOKEN_EXPIRADO"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno durante a validação",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidateTokenError.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                        {
                          "error": "ERRO_INTERNO"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(
        @Parameter(description = "Dados para validação do token 2FA", required = true)
        @RequestBody ValidateTokenRequest req) {
        
        if (req.getUserId() == null || req.getToken() == null || req.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ValidateTokenError("DADOS_INVALIDOS"));
        }
        
        var result = userTokenService.validateToken(req.getUserId(), req.getToken());
        return switch (result) {
            case OK -> {
                try {
                    UserInfo user = userService.findById(req.getUserId());

                    if (!user.isFirstLoginCompleted()) {
                        user.setFirstLoginCompleted(true);
                        userService.save(user);
                        log.info("Primeiro login completado para usuário: {}", user.getUsername());
                    }

                    String accessToken = jwtService.GenerateToken(user.getUsername());
                    String refreshToken = refreshTokenService.getTokenByUserId(user.getId());
                    if (refreshToken.isEmpty()) {
                        refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();
                    }

                    AuthResponseDTO.UserDetailsDTO userDetails = AuthResponseDTO.UserDetailsDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                            .cpf(user.getCpf())
                            .phone(user.getPhone())
                            .companyId(user.getCompanyId())
                            .link(user.getLink())
                            .tokenTemporary(user.getTokenTemporary())
                            .tokenExpiredAt(user.getTokenExpiredAt())
                            .isReset(user.isReset())
                            .isEnabled(user.isEnabled())
                            .isCreatedByAdmin(user.isCreatedByAdmin())
                            .isPasswordChangedByUser(user.isPasswordChangedByUser())
                            .build();

                    var userData = AuthResponseDTO.builder()
                            .user(userDetails)
                            .permissions(null)
                            .functionalities(null)
                            .build();

                    JwtResponseDTO jwt = JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshToken)
                            .userDetails(userData)
                            .build();
                    yield ResponseEntity.ok(jwt);
                } catch (Exception e) {
                    log.error("Error generating tokens after 2FA validation", e);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ValidateTokenError("ERRO_INTERNO"));
                }
            }
            case INVALID -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ValidateTokenError("TOKEN_INVALIDO"));
            case EXPIRED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ValidateTokenError("TOKEN_EXPIRADO"));
        };
    }

    @Data
    @Schema(description = "Requisição para geração de token 2FA")
    public static class GenerateTokenRequest {
        
        @Schema(
            description = "ID do usuário para o qual o token será gerado",
            example = "1",
            required = true
        )
        private Long userId;
    }

    @Data
    @Schema(description = "Resposta da geração de token 2FA")
    public static class GenerateTokenResponse {
        
        @Schema(
            description = "Token 2FA gerado (código alfanumérico)",
            example = "A1B2C",
            required = true
        )
        private final String token;
        
        @Schema(
            description = "Data e hora de expiração do token (ISO 8601)",
            example = "2024-01-15T10:30:00",
            required = true
        )
        private final String expiresAt;
        
        @JsonCreator
        public GenerateTokenResponse(
            @JsonProperty("token") String token,
            @JsonProperty("expiresAt") String expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }

    @Data
    @Schema(description = "Requisição para validação de token 2FA")
    public static class ValidateTokenRequest {
        
        @Schema(
            description = "ID do usuário que está validando o token",
            example = "1",
            required = true
        )
        private Long userId;
        
        @Schema(
            description = "Token 2FA a ser validado",
            example = "A1B2C",
            required = true,
            minLength = 5,
            maxLength = 10
        )
        private String token;
    }

    @Data
    @Schema(description = "Resposta de erro na validação de token 2FA")
    public static class ValidateTokenError {
        
        @Schema(
            description = "Código do erro ocorrido durante a validação",
            example = "TOKEN_INVALIDO",
            required = true,
            allowableValues = {"TOKEN_INVALIDO", "TOKEN_EXPIRADO", "ERRO_INTERNO"}
        )
        private final String error;
        
        @JsonCreator
        public ValidateTokenError(@JsonProperty("error") String error) {
            this.error = error;
        }
    }
}