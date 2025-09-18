package com.montreal.oauth.domain.dto;

import com.montreal.oauth.domain.enumerations.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {

    private Integer id;
    private RoleEnum name;
    private Boolean requiresTokenFirstLogin;
    private Boolean biometricValidation;
    private Boolean tokenLogin;

    private List<Long> permissionIds; 		// IDs das permissões associadas
    private List<Long> functionalityIds; 	// IDs das funcionalidades associadas
}
