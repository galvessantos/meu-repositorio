package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.request.PasswordResetGenerateRequest;
import com.montreal.oauth.domain.dto.request.PasswordResetRequest;
import com.montreal.oauth.domain.dto.response.PasswordResetGenerateResponse;
import com.montreal.oauth.domain.dto.response.PasswordResetResponse;
import com.montreal.oauth.domain.dto.response.PasswordResetValidateResponse;
import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.service.IPasswordResetService;
import com.montreal.core.domain.exception.UserNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Tag(
        name = "Password Reset",
        description = "Funcionalidades para redefinição de senha do usuário. Permite solicitar redefinição, validar tokens e redefinir senhas de forma segura."
)
public class PasswordResetController {

    private final IPasswordResetService passwordResetService;

    @Operation(
            summary = "Solicitar redefinição de senha",
            description = """
                Gera um token único para redefinição de senha e retorna um link de redefinição.
                
                **Fluxo:**
                1. Usuário informa o login (username)
                2. Sistema valida se o login existe no sistema
                3. Se válido, gera token único com expiração de 30 minutos
                4. Retorna link para redefinição
                
                **Observações:**
                - Tokens existentes do usuário são invalidados automaticamente
                - O link gerado deve ser enviado por e-mail (implementação separada)
                - Token expira em 30 minutos por padrão
                - Validação é feita APENAS pelo login (username), não por email
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token gerado com sucesso. Link de redefinição retornado.",
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetGenerateResponse.class),
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    summary = "Token gerado com sucesso",
                                    value = """
                                {
                                  "message": "Password reset token generated successfully",
                                  "resetLink": "https://localhost:5173/reset-password?token=uuid-123"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (login vazio ou formato incorreto)",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Login inválido",
                                    summary = "Validação falhou",
                                    value = """
                                {
                                  "timestamp": "2024-01-15T10:30:00",
                                  "status": 400,
                                  "errors": ["Login is required"]
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Login informado não existe no sistema",
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetGenerateResponse.class),
                            examples = @ExampleObject(
                                    name = "Login não encontrado",
                                    summary = "Usuário não existe",
                                    value = """
                                {
                                  "message": "Login informado inválido",
                                  "resetLink": null
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/generate")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PasswordResetGenerateResponse> generatePasswordResetToken(
            @Valid @RequestBody PasswordResetGenerateRequest request) {

        log.info("Generating password reset token for login: {}", request.getLogin());

        try {
            String resetLink = passwordResetService.generatePasswordResetToken(request.getLogin());

            PasswordResetGenerateResponse response = PasswordResetGenerateResponse.builder()
                    .message("Password reset token generated successfully")
                    .resetLink(resetLink)
                    .build();

            log.info("Password reset token generated successfully for login: {}", request.getLogin());
            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            log.warn("Login not found: {}", request.getLogin());
            PasswordResetGenerateResponse response = PasswordResetGenerateResponse.builder()
                    .message("Login informado inválido")
                    .resetLink(null)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Error generating password reset token for login: {}", request.getLogin(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Validar token de redefinição de senha",
            description = """
                Valida se um token de redefinição de senha é válido e não expirou.
                
                **Validações:**
                - Token deve existir no sistema
                - Token não deve ter expirado (30 minutos)
                - Token não deve ter sido usado anteriormente
                
                **Uso:**
                - Chamado pelo frontend antes de exibir tela de redefinição
                - Garante que o usuário pode prosseguir com a redefinição
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetValidateResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Token válido",
                                            summary = "Token pode ser usado",
                                            value = """
                                    {
                                      "valid": true,
                                      "message": "Token is valid"
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Token inválido",
                                            summary = "Token expirado ou já usado",
                                            value = """
                                    {
                                      "valid": false,
                                      "message": "Token is invalid or expired"
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token não informado ou formato inválido"
            )
    })
    @GetMapping("/validate")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PasswordResetValidateResponse> validatePasswordResetToken(
            @Parameter(
                    description = "Token de redefinição de senha recebido por e-mail",
                    required = true,
                    example = "uuid-123-456-789"
            )
            @RequestParam String token) {

        log.debug("Validating password reset token: {}", token);

        try {
            boolean isValid = passwordResetService.validatePasswordResetToken(token);

            PasswordResetValidateResponse response = PasswordResetValidateResponse.builder()
                    .valid(isValid)
                    .message(isValid ? "Token is valid" : "Token is invalid or expired")
                    .build();

            log.debug("Password reset token validation completed. Valid: {}", isValid);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating password reset token: {}", token, e);
            throw e;
        }
    }

    @Operation(
            summary = "Limpar tokens expirados",
            description = """
                Remove tokens de redefinição de senha expirados e já utilizados.
                
                **Execução:**
                - Automática via scheduler (diariamente às 2h da manhã)
                - Manual via endpoint (para administradores)
                - Remove tokens com mais de 30 minutos
                
                **Segurança:**
                - Apenas tokens expirados são removidos
                - Tokens válidos e não expirados são preservados
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Limpeza realizada com sucesso",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    summary = "Tokens expirados removidos",
                                    value = "Cleanup completed successfully"
                            )
                    )
            )
    })
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");

        try {
            passwordResetService.cleanupExpiredTokens();

            log.info("Cleanup of expired password reset tokens completed successfully");
            return ResponseEntity.ok("Cleanup completed successfully");

        } catch (Exception e) {
            log.error("Error during cleanup of expired tokens", e);
            throw e;
        }
    }

    @Operation(
            summary = "Redefinir senha",
            description = """
                Redefine a senha do usuário usando um token válido.
                
                **Validações de senha:**
                - Tamanho: mínimo 4, máximo 8 caracteres
                - Composição: pelo menos 1 letra maiúscula e 1 minúscula
                - Caracteres especiais: pelo menos 1 dos seguintes: _ @ #
                - Números: pelo menos 1 dígito
                
                **Exemplo de senha válida:** `Nova@123`
                
                **Fluxo:**
                1. Valida se o token é válido e não expirou
                2. Valida se as senhas coincidem
                3. Valida critérios de complexidade da nova senha
                4. Criptografa e salva a nova senha
                5. Marca o token como usado
                6. Ativa a conta do usuário
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Senha redefinida com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetResponse.class),
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    summary = "Senha alterada com sucesso",
                                    value = """
                                {
                                  "message": "Senha redefinida com sucesso",
                                  "success": true
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou validação de senha falhou",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Senhas não coincidem",
                                            summary = "Confirmação falhou",
                                            value = """
                                    {
                                      "message": "As senhas não coincidem",
                                      "success": false
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Critérios de senha não atendidos",
                                            summary = "Validação de complexidade falhou",
                                            value = """
                                    {
                                      "message": "A senha deve conter pelo menos uma letra maiúscula",
                                      "success": false
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Token inválido ou expirado",
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetResponse.class),
                            examples = @ExampleObject(
                                    name = "Token inválido",
                                    summary = "Token não pode ser usado",
                                    value = """
                                {
                                  "message": "Token inválido ou expirado",
                                  "success": false
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/reset")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        log.info("Attempting to reset password with token: {}", request.getToken());

        try {
            ResetPasswordResult result = passwordResetService.resetPasswordWithTokens(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            if (result.isSuccess()) {
                PasswordResetResponse response = PasswordResetResponse.builder()
                        .message(result.getMessage())
                        .success(true)
                        .accessToken(result.getAccessToken())
                        .refreshToken(result.getRefreshToken())
                        .userDetails(result.getUserDetails())
                        .requiresToken(false)
                        .build();

                log.info("Password reset completed successfully with auto-login: {}",
                        result.getAccessToken() != null);
                return ResponseEntity.ok(response);
            } else {
                PasswordResetResponse response = PasswordResetResponse.builder()
                        .message(result.getMessage())
                        .success(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Error during password reset", e);
            PasswordResetResponse response = PasswordResetResponse.builder()
                    .message("Erro interno do servidor")
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}