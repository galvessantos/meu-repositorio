package com.montreal.oauth.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RolePermissionsRequest {

    @NotNull(message = "O ID da role é obrigatório")
    private Integer roleId;

    @NotNull(message = "A lista de permissões não pode ser nula")
    @Size(min = 1, message = "A lista de permissões deve conter pelo menos uma permissão")
    private List<Long> permissionIds;
}

