package com.montreal.msiav_bh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.montreal.msiav_bh.dto.request.ProbableAddressRequest;
import com.montreal.msiav_bh.dto.response.ProbableAddressResponse;
import com.montreal.msiav_bh.entity.ProbableAddress;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface ProbableAddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle.id", source = "vehicleId")
    ProbableAddress toEntity(ProbableAddressRequest request);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    ProbableAddressResponse toResponse(ProbableAddress entity);
}