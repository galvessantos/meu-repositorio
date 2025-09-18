package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.VehicleImagesDTO;
import com.montreal.msiav_bh.dto.request.VehicleImagesRequest;
import com.montreal.msiav_bh.dto.response.VehicleImagesResponse;
import com.montreal.msiav_bh.entity.VehicleImages;

@Mapper(componentModel = "spring", uses = {VehicleMapper.class, VehicleSeizureMapper.class})
public interface VehicleImagesMapper {

    VehicleImagesMapper INSTANCE = Mappers.getMapper(VehicleImagesMapper.class);

    @Mapping(target = "id", ignore = true)
    VehicleImages toEntity(VehicleImagesRequest request);

    VehicleImagesResponse toResponse(VehicleImages entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(VehicleImagesRequest request, @MappingTarget VehicleImages entity);

    VehicleImages toEntity(VehicleImagesDTO dto);

    VehicleImagesDTO toDto(VehicleImages entity);

    List<VehicleImagesResponse> toResponseList(List<VehicleImages> entities);

    List<VehicleImagesDTO> toDtoList(List<VehicleImages> entities);
}
