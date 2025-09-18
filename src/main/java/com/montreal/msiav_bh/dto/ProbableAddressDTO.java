package com.montreal.msiav_bh.dto;

import com.montreal.msiav_bh.entity.Address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbableAddressDTO {

    @Schema(description = "Identificador único da associação de endereço provável")
    private Long id;

    @Schema(description = "ID do veículo associado")
    private Long vehicleId;

    @Schema(description = "Dados completos do endereço associado")
    private Address address;
}
