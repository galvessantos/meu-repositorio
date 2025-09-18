package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.WitnessesDTO;
import com.montreal.msiav_bh.dto.request.WitnessesRequest;
import com.montreal.msiav_bh.dto.response.WitnessesResponse;
import com.montreal.msiav_bh.entity.Witnesses;

@Mapper(componentModel = "spring")
public interface WitnessesMapper {

    WitnessesMapper INSTANCE = Mappers.getMapper(WitnessesMapper.class);

    @Mapping(target = "id", ignore = true)
    Witnesses toEntity(WitnessesRequest request);

    WitnessesResponse toResponse(Witnesses entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(WitnessesRequest request, @MappingTarget Witnesses entity);

    Witnesses toEntity(WitnessesDTO dto);

    WitnessesDTO toDto(Witnesses entity);

    List<WitnessesResponse> toResponseList(List<Witnesses> entities);

    List<WitnessesDTO> toDtoList(List<Witnesses> entities);
}
