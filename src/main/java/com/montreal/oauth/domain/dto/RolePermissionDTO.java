package com.montreal.oauth.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolePermissionDTO {

    @NotNull(message = "O ID da role é obrigatório.")
    private Long roleId;

    @NotNull(message = "O ID da permissão é obrigatório.")
    private Long permissionId;
    
}