package com.montreal.integration.response;

import java.util.List;
import com.montreal.msiav_bh.dto.HistoryRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.dto.response.VehicleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDataResponse {

    @Schema(description = "Dados do veículo")
    private VehicleResponse vehicleData;

    @Schema(description = "Endereços associados ao veículo")
    private List<AddressResponse> addresses;

    @Schema(description = "Empresas associadas ao veículo")
    private List<CompanyResponse> companies;

    @Schema(description = "Históricos do veículo")
    private List<HistoryRequest> histories;

}
