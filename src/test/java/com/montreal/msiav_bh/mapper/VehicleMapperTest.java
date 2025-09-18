package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.request.VehicleRequest;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.enumerations.VehicleStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMapperTest {

    @Test
    @DisplayName("Should set new vehicle status and stage with new values")
    void shouldSetNewVehicleStatusAndStageWithNewValues() {
        Vehicle vehicle = Vehicle.builder()
                .status(VehicleStatusEnum.A_INICIAR)
                .stage(VehicleStageEnum.CERTIDAO_BUSCA_APREENSAO_EMITIDA)
                .build();

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .stage(VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR)
                .build();

        VehicleMapper.INSTANCE.merge(vehicle, vehicleRequest);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusEnum.FINALIZADO);
        assertThat(vehicle.getStage()).isEqualTo(VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR);
    }

    @Test
    @DisplayName("Should set new vehicle status and stage with actual values")
    void shouldSetNewVehicleStatusWithActualValues() {
        Vehicle vehicle = Vehicle.builder()
                .status(VehicleStatusEnum.VEICULO_LOCALIZADO)
                .stage(VehicleStageEnum.VEICULO_RECOLHIDO)
                .build();

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .stage(VehicleStageEnum.CERTIDAO_BUSCA_APREENSAO_EMITIDA)
                .build();

        VehicleMapper.INSTANCE.merge(vehicle, vehicleRequest);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusEnum.VEICULO_LOCALIZADO);
        assertThat(vehicle.getStage()).isEqualTo(VehicleStageEnum.VEICULO_RECOLHIDO);
    }

    @Test
    @DisplayName("Should set new vehicle status and stage with actual values canceling")
    void shouldSetNewVehicleStatusWithActualValuesCanceling() {
        Vehicle vehicle = Vehicle.builder()
                .status(VehicleStatusEnum.FINALIZADO)
                .stage(VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR)
                .build();

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .stage(VehicleStageEnum.CERTIDAO_BUSCA_APREENSAO_EMITIDA)
                .build();

        VehicleMapper.INSTANCE.merge(vehicle, vehicleRequest);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusEnum.FINALIZADO);
        assertThat(vehicle.getStage()).isEqualTo(VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR);
    }

}