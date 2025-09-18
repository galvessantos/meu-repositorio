package com.montreal.msiav_bh.dto.request;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.TypeHistoryEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRequest {

    @NotNull(message = "O ID do veículo é obrigatório.")
    private Long vehicleId;

    @NotBlank(message = "A placa do veículo é obrigatória.")
    private String licensePlate;

    private String model;

    private String creditorName;

    private String contractNumber;

    private LocalDateTime creationDateTime;

    private TypeHistoryEnum typeHistory;

    private LocationRequest location;

    private CollectedRequest collected;

    private ImpoundLotRequest impoundLot;
}
