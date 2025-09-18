package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.UserVehicleAssociationDTO;
import com.montreal.msiav_bh.dto.request.UserVehicleAssociationRequest;
import com.montreal.msiav_bh.dto.response.UserVehicleAssociationResponse;
import com.montreal.msiav_bh.entity.UserVehicleAssociation;

@Mapper(componentModel = "spring", uses = {VehicleMapper.class})
public interface UserVehicleAssociationMapper {

    UserVehicleAssociationMapper INSTANCE = Mappers.getMapper(UserVehicleAssociationMapper.class);

    @Mapping(target = "id", ignore = true)
    UserVehicleAssociation toEntity(UserVehicleAssociationRequest request);

    UserVehicleAssociationResponse toResponse(UserVehicleAssociation entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UserVehicleAssociationRequest request, @MappingTarget UserVehicleAssociation entity);

    UserVehicleAssociation toEntity(UserVehicleAssociationDTO dto);

    UserVehicleAssociationDTO toDto(UserVehicleAssociation entity);

    List<UserVehicleAssociationResponse> toResponseList(List<UserVehicleAssociation> entities);

    List<UserVehicleAssociationDTO> toDtoList(List<UserVehicleAssociation> entities);
}
