package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.CompanyDTO;
import com.montreal.msiav_bh.dto.request.CompanyRequest;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.entity.Company;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    @Mapping(target = "id", ignore = true)
    Company toEntity(CompanyRequest request);

    CompanyResponse toResponse(Company entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget Company entity);

    Company toEntity(CompanyDTO dto);

    CompanyDTO toDto(Company entity);

    List<CompanyResponse> toResponseList(List<Company> entities);

    List<CompanyDTO> toDtoList(List<Company> entities);
}
