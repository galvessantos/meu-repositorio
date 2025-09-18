package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.dto.CompanyDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCompanyResponse {
	
    private Long id;
    private Long companyId;
    private Long vehicleId;
    private VehicleResponse vehicle;
    private CompanyDTO company;
    private LocalDateTime createdAt;
    private String companyType;
}
