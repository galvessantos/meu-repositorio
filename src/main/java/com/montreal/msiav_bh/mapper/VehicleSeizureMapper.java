package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.montreal.msiav_bh.dto.VehicleSeizureDTO;
import com.montreal.msiav_bh.dto.request.VehicleSeizureRequest;
import com.montreal.msiav_bh.dto.response.VehicleSeizureResponse;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.entity.VehicleSeizure;
import com.montreal.oauth.mapper.IUserMapper;

@Mapper(componentModel = "spring", uses = {IUserMapper.class, AddressMapper.class, CompanyMapper.class})
public interface VehicleSeizureMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "vehicle", source = "vehicle")
    VehicleSeizure toEntity(VehicleSeizureRequest request, Vehicle vehicle);

    VehicleSeizureResponse toResponse(VehicleSeizure entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(VehicleSeizureRequest request, @MappingTarget VehicleSeizure entity);

    VehicleSeizure toEntity(VehicleSeizureDTO dto);

    VehicleSeizureDTO toDto(VehicleSeizure entity);

    List<VehicleSeizureResponse> toResponseList(List<VehicleSeizure> entities);

    List<VehicleSeizureDTO> toDtoList(List<VehicleSeizure> entities);
}
