package com.montreal.oauth.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.montreal.oauth.domain.dto.RoleDTO;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.montreal.oauth.domain.dto.request.UserRequest;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    IUserMapper INSTANCE = Mappers.getMapper(IUserMapper.class);

    UserInfo toEntity(UserRequest userRequest);

    // Método default completo para evitar problemas de geração
    default UserResponse toResponse(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(userInfo.getId());
        response.setUsername(userInfo.getUsername());
        response.setFullName(userInfo.getFullName());
        response.setEmail(userInfo.getEmail());
        response.setLink(userInfo.getLink());
        response.setResetAt(userInfo.getResetAt());
        response.setCompanyId(userInfo.getCompanyId());
        response.setPhone(userInfo.getPhone());
        response.setCpf(userInfo.getCpf());
        response.setEnabled(userInfo.isEnabled());
        response.setReset(userInfo.isReset());
        response.setCreatedByAdmin(userInfo.isCreatedByAdmin());
        response.setPasswordChangedByUser(userInfo.isPasswordChangedByUser());

        // Mapeamento manual das roles - Convert Set<Role> para List<RoleDTO>
        if (userInfo.getRoles() != null) {
            List<RoleDTO> roleDTOs = userInfo.getRoles().stream()
                    .map(this::toRoleDTO)
                    .collect(Collectors.toList());
            response.setRoles(roleDTOs);
        }

        return response;
    }

    default RoleDTO toRoleDTO(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO response = new RoleDTO();
        response.setId(role.getId());
        response.setName(role.getName() != null ? RoleEnum.valueOf(role.getName().name()) : null);
        response.setRequiresTokenFirstLogin(role.getRequiresTokenFirstLogin());
        response.setBiometricValidation(role.getBiometricValidation());
        response.setTokenLogin(role.getTokenLogin());

        // ✅ Use o getter customizado que já testamos
        response.setFunctionalityIds(role.getFunctionalityIds());

        return response;
    }
}