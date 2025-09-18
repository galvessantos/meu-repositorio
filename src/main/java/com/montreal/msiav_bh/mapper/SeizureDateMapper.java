package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.SeizureDateDTO;
import com.montreal.msiav_bh.dto.request.SeizureDateRequest;
import com.montreal.msiav_bh.dto.response.SeizureDateResponse;
import com.montreal.msiav_bh.entity.SeizureDate;

@Mapper(componentModel = "spring", uses = VehicleMapper.class)
public interface SeizureDateMapper {

    SeizureDateMapper INSTANCE = Mappers.getMapper(SeizureDateMapper.class);

    @Mapping(target = "id", ignore = true)
    SeizureDate toEntity(SeizureDateRequest request);

    SeizureDateResponse toResponse(SeizureDate entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(SeizureDateRequest request, @MappingTarget SeizureDate entity);

    SeizureDate toEntity(SeizureDateDTO dto);

    SeizureDateDTO toDto(SeizureDate entity);

    List<SeizureDateResponse> toResponseList(List<SeizureDate> entities);

    List<SeizureDateDTO> toDtoList(List<SeizureDate> entities);
}
