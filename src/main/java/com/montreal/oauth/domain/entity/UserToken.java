package com.montreal.oauth.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa um token de autenticação 2FA para um usuário")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do token", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuário proprietário do token")
    private UserInfo user;

    @Column(name = "token", length = 10, nullable = false)
    @Schema(description = "Código do token 2FA", example = "A1B2C", maxLength = 10)
    private String token;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Data e hora de criação do token", example = "2024-01-15T10:25:00")
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    @Schema(description = "Data e hora de expiração do token", example = "2024-01-15T10:30:00")
    private LocalDateTime expiresAt;

    @Column(name = "is_valid", nullable = false)
    @Schema(description = "Indica se o token ainda é válido", example = "true")
    private Boolean isValid;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isValid == null) {
            isValid = true;
        }
    }
}
