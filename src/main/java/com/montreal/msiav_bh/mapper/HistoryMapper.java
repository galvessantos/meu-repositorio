package com.montreal.msiav_bh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.montreal.msiav_bh.dto.HistoryRequest;
import com.montreal.msiav_bh.dto.response.HistoryResponse;
import com.montreal.msiav_bh.entity.History;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    HistoryMapper INSTANCE = Mappers.getMapper(HistoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle.id", source = "idVehicle")
    @Mapping(target = "location.vehicleFound", source = "location.vehicleFound")
    @Mapping(target = "location.note", source = "location.note")
    @Mapping(target = "collected.vehicleFound", source = "collected.vehicleFound")
    @Mapping(target = "collected.note", source = "collected.note")
    @Mapping(target = "collected.collectionDateTime", source = "collected.collectionDateTime")
    @Mapping(target = "impoundLot.impoundArrivalDateTime", source = "impoundLot.impoundArrivalDateTime")
    @Mapping(target = "impoundLot.impoundDepartureDateTime", source = "impoundLot.impoundDepartureDateTime")
    History toEntity(HistoryRequest request);

    @Mapping(target = "idVehicle", source = "vehicle.id")
    @Mapping(target = "location.vehicleFound", source = "location.vehicleFound")
    @Mapping(target = "location.note", source = "location.note")
    @Mapping(target = "collected.vehicleFound", source = "collected.vehicleFound")
    @Mapping(target = "collected.note", source = "collected.note")
    @Mapping(target = "collected.collectionDateTime", source = "collected.collectionDateTime")
    @Mapping(target = "impoundLot.impoundArrivalDateTime", source = "impoundLot.impoundArrivalDateTime")
    @Mapping(target = "impoundLot.impoundDepartureDateTime", source = "impoundLot.impoundDepartureDateTime")
    HistoryRequest toDto(History entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle.id", source = "idVehicle")
    @Mapping(target = "location.vehicleFound", source = "location.vehicleFound")
    @Mapping(target = "location.note", source = "location.note")
    @Mapping(target = "collected.vehicleFound", source = "collected.vehicleFound")
    @Mapping(target = "collected.note", source = "collected.note")
    @Mapping(target = "collected.collectionDateTime", source = "collected.collectionDateTime")
    @Mapping(target = "impoundLot.impoundArrivalDateTime", source = "impoundLot.impoundArrivalDateTime")
    @Mapping(target = "impoundLot.impoundDepartureDateTime", source = "impoundLot.impoundDepartureDateTime")
    void updateEntityFromRequest(HistoryRequest request, @MappingTarget History entity);

    List<HistoryResponse> toResponseList(List<History> entities);

    List<HistoryRequest> toDtoList(List<History> entities);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "location.vehicleFound", source = "location.vehicleFound")
    @Mapping(target = "location.note", source = "location.note")
    @Mapping(target = "collected.vehicleFound", source = "collected.vehicleFound")
    @Mapping(target = "collected.note", source = "collected.note")
    @Mapping(target = "collected.collectionDateTime", source = "collected.collectionDateTime")
    @Mapping(target = "impoundLot.impoundArrivalDateTime", source = "impoundLot.impoundArrivalDateTime")
    @Mapping(target = "impoundLot.impoundDepartureDateTime", source = "impoundLot.impoundDepartureDateTime")
    HistoryResponse toResponse(History entity);

}
