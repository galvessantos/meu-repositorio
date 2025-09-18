package com.montreal.oauth.domain.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.montreal.oauth.domain.dto.RoleDTO;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.RoleFunctionality;
import com.montreal.oauth.domain.entity.RolePermission;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    @Mapping(target = "permissionIds", source = "rolePermissions", qualifiedByName = "mapPermissionIds")
    @Mapping(target = "functionalityIds", source = "roleFunctionalities", qualifiedByName = "mapFunctionalityIds")
    RoleDTO toDTO(Role entity);

    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "roleFunctionalities", ignore = true)
    Role toEntity(RoleDTO dto);

    List<RoleDTO> toDTOList(List<Role> entities);

    @Named("mapPermissionIds")
    static List<Long> mapPermissionIds(Set<RolePermission> rolePermissions) {
        return rolePermissions != null ? rolePermissions.stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toList()) : null;
    }

    @Named("mapFunctionalityIds")
    static List<Long> mapFunctionalityIds(Set<RoleFunctionality> roleFunctionalities) {
        return roleFunctionalities != null ? roleFunctionalities.stream()
                .map(rf -> rf.getFunctionality().getId())
                .collect(Collectors.toList()) : null;
    }
}
