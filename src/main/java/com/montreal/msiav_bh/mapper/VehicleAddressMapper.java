package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.VehicleAddressDTO;
import com.montreal.msiav_bh.dto.request.VehicleAddressRequest;
import com.montreal.msiav_bh.dto.response.VehicleAddressResponse;
import com.montreal.msiav_bh.entity.VehicleAddress;

@Mapper(componentModel = "spring", uses = {VehicleMapper.class, AddressMapper.class})
public interface VehicleAddressMapper {

    VehicleAddressMapper INSTANCE = Mappers.getMapper(VehicleAddressMapper.class);

    @Mapping(target = "id", ignore = true)
    VehicleAddress toEntity(VehicleAddressRequest request);

    VehicleAddressResponse toResponse(VehicleAddress entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(VehicleAddressRequest request, @MappingTarget VehicleAddress entity);

    VehicleAddress toEntity(VehicleAddressDTO dto);

    VehicleAddressDTO toDto(VehicleAddress entity);

    List<VehicleAddressResponse> toResponseList(List<VehicleAddress> entities);

    List<VehicleAddressDTO> toDtoList(List<VehicleAddress> entities);
}
