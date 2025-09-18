package com.montreal.core.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Objeto de transferência para registro de auditoria")
public class RegisterAuditDTO {

    @Schema(description = "Identificador único do log", example = "12345")
    private String id;

    @Schema(description = "Identificação do usuário que realizou a ação", example = "67890")
    private Long userId;

    @Schema(description = "Descrição da ação executada", example = "Atualização de perfil")
    private String action;

    @Schema(description = "Data e hora em que a ação ocorreu", example = "2025-02-22T13:52:03")
    private LocalDateTime timestamp;

    @Schema(description = "Resultado da ação", example = "sucesso")
    private RegisterAuditStatusEnum status;

    @Schema(description = "Informações adicionais ou contexto sobre a ação executada", example = "Atualização realizada pelo administrador")
    private String description;

}