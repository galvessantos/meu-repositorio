package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.SystemParameterDTO;
import com.montreal.msiav_bh.dto.request.SystemParameterRequest;
import com.montreal.msiav_bh.dto.response.SystemParameterResponse;
import com.montreal.msiav_bh.entity.SystemParameter;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SystemParameterMapper {

    SystemParameterMapper INSTANCE = Mappers.getMapper(SystemParameterMapper.class);

    @Mapping(target = "id", ignore = true)
    SystemParameter toEntity(SystemParameterRequest request);

    SystemParameterResponse toResponse(SystemParameter entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(SystemParameterRequest request, @MappingTarget SystemParameter entity);

    SystemParameter toEntity(SystemParameterDTO dto);

    SystemParameterDTO toDto(SystemParameter entity);

    List<SystemParameterResponse> toResponseList(List<SystemParameter> entities);

    List<SystemParameterDTO> toDtoList(List<SystemParameter> entities);
}
