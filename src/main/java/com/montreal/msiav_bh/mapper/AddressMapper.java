package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.request.AddressRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequest request);

    AddressResponse toResponse(Address entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(AddressRequest request, @MappingTarget Address entity);

    List<AddressResponse> toResponseList(List<Address> entities);

}
