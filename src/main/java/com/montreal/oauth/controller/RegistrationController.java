package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.request.FirstPasswordRequest;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.domain.service.UserTokenService;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Registro", description = "Fluxo de primeiro cadastro de senha (primeiro acesso)")
public class RegistrationController {

    private final UserService userService;
    private final UserTokenService userTokenService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Definir senha no primeiro acesso")
    @PostMapping("/first-password")
    public ResponseEntity<?> setFirstPassword(@Valid @RequestBody FirstPasswordRequest request) {
        try {
            UserInfo user = userService.getUserById(request.getUserId());

            if (user.isPasswordChangedByUser()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Senha já foi definida anteriormente");
            }

            // Define a primeira senha
            userService.changePassword(user.getId(), request.getPassword());

            // Se a role exigir token no primeiro acesso, gerar e sinalizar ao cliente
            boolean requiresFirstToken = user.getRoles().stream().anyMatch(r -> Boolean.TRUE.equals(r.getRequiresTokenFirstLogin()));
            if (requiresFirstToken) {
                userTokenService.generateAndPersist(userService.getUserById(user.getId()));
                JwtResponseDTO response = JwtResponseDTO.builder()
                        .accessToken(null)
                        .token(null)
                        .userDetails(null)
                        .requiresToken(true)
                        .build();
                return ResponseEntity.ok(response);
            }

            // Caso não exija token no primeiro acesso, retorna tokens de login automático
            String accessToken = jwtService.GenerateToken(user.getUsername());
            String refreshToken = refreshTokenService.getTokenByUserId(user.getId());
            if (refreshToken.isEmpty()) {
                refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();
            }

            JwtResponseDTO response = JwtResponseDTO.builder()
                    .accessToken(accessToken)
                    .token(refreshToken)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao definir primeira senha", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao definir primeira senha");
        }
    }
}

