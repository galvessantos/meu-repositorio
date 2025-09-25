package com.montreal.oauth.controller;

import com.montreal.oauth.domain.dto.AuthResponseDTO;
import com.montreal.oauth.domain.dto.JwtResponseDTO;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.RefreshTokenService;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.domain.service.UserTokenService;
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
public class AuthTokenController {

    private final UserService userService;
    private final UserTokenService userTokenService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/generate-token")
    public ResponseEntity<GenerateTokenResponse> generateToken(@RequestBody GenerateTokenRequest req) {
        UserInfo user = userService.findById(req.getUserId());
        var token = userTokenService.generateAndPersist(user);
        return ResponseEntity.ok(new GenerateTokenResponse(token.getToken(), token.getExpiresAt().toString()));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest req) {
        var result = userTokenService.validateToken(req.getUserId(), req.getToken());
        return switch (result) {
            case OK -> {
                try {
                    UserInfo user = userService.findById(req.getUserId());
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
    public static class GenerateTokenRequest {
        private Long userId;
    }

    @Data
    public static class GenerateTokenResponse {
        private final String token;
        private final String expiresAt;
    }

    @Data
    public static class ValidateTokenRequest {
        private Long userId;
        private String token;
    }

    @Data
    public static class ValidateTokenError {
        private final String error;
    }
}
