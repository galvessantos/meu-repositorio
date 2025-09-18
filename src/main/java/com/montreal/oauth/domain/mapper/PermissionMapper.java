package com.montreal.oauth.domain.mapper;

import com.montreal.oauth.domain.dto.PermissionDTO;
import com.montreal.oauth.domain.dto.response.PermissionResponseDTO;
import com.montreal.oauth.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Permission toEntity(PermissionDTO dto);

    PermissionResponseDTO toResponseDTO(Permission permission);

    List<PermissionResponseDTO> toResponseDTOList(List<Permission> permissions);
}
