package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.factory.Mappers;

import com.montreal.core.domain.dto.RegisterAuditDTO;
import com.montreal.msiav_bh.entity.RegisterAudit;

public interface IAuditMapper {

	IAuditMapper INSTANCE = Mappers.getMapper(IAuditMapper.class);

	RegisterAudit toEntity(RegisterAuditDTO registerAuditDTO);

    RegisterAuditDTO toDTO(RegisterAudit registerAuditCollection);

    List<RegisterAuditDTO> toCollectionDTO(List<RegisterAudit> registerAuditCollection);
}
