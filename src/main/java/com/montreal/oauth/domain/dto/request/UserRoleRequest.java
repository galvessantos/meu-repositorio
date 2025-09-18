package com.montreal.oauth.domain.dto.request;

import com.montreal.oauth.domain.enumerations.RoleEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRequest {

    private Integer id;
    private RoleEnum name;
    private Boolean requiresTokenFirstLogin;
    private Boolean biometricValidation;
    private Boolean tokenLogin;

}
