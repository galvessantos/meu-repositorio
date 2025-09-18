package com.montreal.msiav_bh.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAddressRequest {

    @NotNull(message = "O ID do veículo é obrigatório.")
    private Long vehicleId;

    @NotNull(message = "O ID do endereço é obrigatório.")
    private Long addressId;

    @NotNull(message = "A data de associação é obrigatória.")
    private LocalDateTime associatedDate;
}
