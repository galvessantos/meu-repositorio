package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import com.montreal.core.domain.dto.RegisterAuditStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "register_audit", indexes = {
        @Index(name = "index_userId", columnList = "user_id")
})
public class RegisterAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O ID do usuário não pode ser nulo")
    @Column(name = "user_id", nullable = false)
    private Long userId; // ID do usuário que realizou a ação

    @NotBlank(message = "A ação não pode estar vazia")
    @Column(name = "action", nullable = false)
    private String action; // Ação realizada

    @NotNull(message = "O status não pode ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegisterAuditStatusEnum status; // Status do registro

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Descrição adicional

    @Column(name = "ip_address")
    private String ipAddress; // Endereço IP do usuário

    @NotNull(message = "O timestamp não pode ser nulo")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp; // Data e hora do evento
}
