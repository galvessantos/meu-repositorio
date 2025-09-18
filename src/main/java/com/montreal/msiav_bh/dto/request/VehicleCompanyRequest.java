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
public class VehicleCompanyRequest {
	
	private Long id;

    @NotNull(message = "O ID do veículo é obrigatório.")
    private Long vehicleId;

    @NotNull(message = "O ID da empresa é obrigatório.")
    private Long companyId;

    private LocalDateTime dateTrigger;
    
    @NotNull(message = "O tipo da empresa é obrigatório.")
    private String companyType;
    
}
