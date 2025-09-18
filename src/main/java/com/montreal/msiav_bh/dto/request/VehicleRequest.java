package com.montreal.msiav_bh.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.FuelTypeEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.enumerations.VehicleStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequest {

    @NotBlank(message = "A placa do veículo é obrigatória.")
    private String licensePlate;

    private String model;

    private String registrationState;

    private String creditorName;

    private String contractNumber;

    private String renavam;

    private String chassisNumber;

    private FuelTypeEnum fuelType;

    private Integer manufactureYear;

    private Integer modelYear;

    private VehicleStageEnum stage;

    private VehicleStatusEnum status;

    @PastOrPresent(message = "A data da solicitação não pode estar no futuro.")
    private LocalDate requestDate;

    private LocalDateTime seizureDateTime;

    private LocalDateTime lastMovementDate;

    private LocalDateTime seizureScheduledDate;
}
