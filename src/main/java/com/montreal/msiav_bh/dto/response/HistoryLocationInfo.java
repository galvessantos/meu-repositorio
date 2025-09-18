package com.montreal.msiav_bh.dto.response;

import com.montreal.msiav_bh.enumerations.TypeHistoryEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLocationInfo {

    @Schema(description = "Endereço onde o veículo foi encontrado")
    private AddressResponse address;

    @Schema(description = "Tipo de histórico")
    private TypeHistoryEnum typeHistory;
}
