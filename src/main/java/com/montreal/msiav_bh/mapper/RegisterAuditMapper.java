package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.core.domain.dto.RegisterAuditDTO;
import com.montreal.msiav_bh.dto.request.RegisterAuditRequest;
import com.montreal.msiav_bh.dto.response.RegisterAuditResponse;
import com.montreal.msiav_bh.entity.RegisterAudit;

@Mapper(componentModel = "spring")
public interface RegisterAuditMapper {

    RegisterAuditMapper INSTANCE = Mappers.getMapper(RegisterAuditMapper.class);

    @Mapping(target = "id", ignore = true)
    RegisterAudit toEntity(RegisterAuditRequest request);

    RegisterAuditResponse toResponse(RegisterAudit entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(RegisterAuditRequest request, @MappingTarget RegisterAudit entity);

    RegisterAudit toEntity(RegisterAuditDTO dto);

    RegisterAuditDTO toDto(RegisterAudit entity);

    List<RegisterAuditResponse> toResponseList(List<RegisterAudit> entities);

    List<RegisterAuditDTO> toDtoList(List<RegisterAudit> entities);
}
