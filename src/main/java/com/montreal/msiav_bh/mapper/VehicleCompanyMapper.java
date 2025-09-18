package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.VehicleCompanyDTO;
import com.montreal.msiav_bh.dto.request.VehicleCompanyRequest;
import com.montreal.msiav_bh.dto.response.VehicleCompanyResponse;
import com.montreal.msiav_bh.entity.VehicleCompany;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {VehicleMapper.class, CompanyMapper.class})
public interface VehicleCompanyMapper {

    VehicleCompanyMapper INSTANCE = Mappers.getMapper(VehicleCompanyMapper.class);

    @Mapping(target = "id", ignore = true)
    VehicleCompany toEntity(VehicleCompanyRequest request);

    VehicleCompanyResponse toResponse(VehicleCompany entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(VehicleCompanyRequest request, @MappingTarget VehicleCompany entity);

    VehicleCompany toEntity(VehicleCompanyDTO dto);

    VehicleCompanyDTO toDto(VehicleCompany entity);

    List<VehicleCompanyResponse> toResponseList(List<VehicleCompany> entities);

    List<VehicleCompanyDTO> toDtoList(List<VehicleCompany> entities);
}
