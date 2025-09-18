package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.request.VehicleRequest;
import com.montreal.msiav_bh.dto.response.VehicleResponse;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.enumerations.VehicleStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import static com.montreal.msiav_bh.enumerations.VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    @Mapping(target = "stage", source = "stage", qualifiedByName = "getVehicleStage")
    @Mapping(target = "status", source = "status", qualifiedByName = "getVehicleStatus")
    VehicleResponse toVehicleResponse(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    Vehicle toVehicle(VehicleRequest request);

    @Mapping(target = "status", expression = "java(setNewVehicleStatus(vehicle.getStatus(), vehicleRequest.getStage()))")
    @Mapping(target = "stage", expression = "java(setNewVehicleStage(vehicle.getStage(), vehicleRequest.getStage()))")
    Vehicle merge(@MappingTarget Vehicle vehicle, VehicleRequest vehicleRequest);

    @Named("getVehicleStage")
    default String getVehicleStage(VehicleStageEnum vehicleStageEnum) {
        if (vehicleStageEnum != null) {
            return vehicleStageEnum.getDescription();
        }
        return null;
    }
    @Named("getVehicleStatus")
    default String getVehicleStatus(VehicleStatusEnum vehicleStatusEnum) {
        if (vehicleStatusEnum != null) {
            return vehicleStatusEnum.getDescription();
        }
        return null;
    }


    default VehicleStageEnum setNewVehicleStage(VehicleStageEnum stageEnum, VehicleStageEnum newStageEnum) {
        if (CANCELAMENTO_PEDIDO_CREDOR == stageEnum) {
            return stageEnum;
        }

        if (CANCELAMENTO_PEDIDO_CREDOR == newStageEnum) {
            return newStageEnum;
        }

        return stageEnum;
    }

    default VehicleStatusEnum setNewVehicleStatus(VehicleStatusEnum statusEnum, VehicleStageEnum newStageEnum) {
        if (VehicleStatusEnum.FINALIZADO == statusEnum) {
            return statusEnum;
        }

        if (CANCELAMENTO_PEDIDO_CREDOR == newStageEnum) {
            return VehicleStatusEnum.FINALIZADO;
        }

        return statusEnum;
    }

}
