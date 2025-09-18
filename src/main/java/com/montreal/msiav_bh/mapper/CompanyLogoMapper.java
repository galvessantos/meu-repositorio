package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.CompanyLogoDTO;
import com.montreal.msiav_bh.dto.request.CompanyLogoRequest;
import com.montreal.msiav_bh.dto.response.CompanyLogoResponse;
import com.montreal.msiav_bh.entity.CompanyLogo;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyLogoMapper {

    CompanyLogoMapper INSTANCE = Mappers.getMapper(CompanyLogoMapper.class);

    @Mapping(target = "id", ignore = true)
    CompanyLogo toEntity(CompanyLogoRequest request);

    CompanyLogoResponse toResponse(CompanyLogo entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(CompanyLogoRequest request, @MappingTarget CompanyLogo entity);

    CompanyLogo toEntity(CompanyLogoDTO dto);

    CompanyLogoDTO toDto(CompanyLogo entity);

    List<CompanyLogoResponse> toResponseList(List<CompanyLogo> entities);

    List<CompanyLogoDTO> toDtoList(List<CompanyLogo> entities);
}
