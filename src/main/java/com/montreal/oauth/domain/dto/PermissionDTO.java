package com.montreal.oauth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionDTO {

    @NotBlank(message = "A ação da permissão é obrigatória.")
    private String action; // Ex: read, write, delete

    @NotBlank(message = "O sujeito da permissão é obrigatório.")
    private String subject; // Ex: User, Vehicle

    private String fields; // Campos específicos da entidade

    private String description; // Descrição da permissão
}
