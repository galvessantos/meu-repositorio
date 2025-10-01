package com.montreal.oauth.domain.entity;

import com.montreal.oauth.domain.enumerations.TwoFactorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "two_factor_auth")
@Schema(description = "Entidade que representa as configurações de autenticação de dois fatores para um usuário")
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único da configuração 2FA", example = "1")
    private Long id;

    @Column(name = "user_id")
    @Schema(description = "ID do usuário proprietário da configuração", example = "1")
    private Long userId;

    @Column(name = "type")
    @Schema(description = "Tipo de autenticação de dois fatores configurado")
    private TwoFactorTypeEnum twoFactorType;

    @Column(name = "is_mandatory")
    @Schema(description = "Indica se o 2FA é obrigatório para este usuário", example = "false")
    private Boolean isMandatory = false;

    @Column(name = "temporary_token")
    @Schema(description = "Token temporário para configuração inicial", example = "temp123")
    private String temporaryToken;

    @Column(name = "expires_at")
    @Schema(description = "Data e hora de expiração do token temporário", example = "2024-01-15T10:30:00")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    @Schema(description = "Data e hora de criação da configuração", example = "2024-01-15T10:00:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data e hora da última atualização", example = "2024-01-15T10:15:00")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}