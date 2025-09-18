package com.montreal.msiav_bh.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCompanyDTO {

	private Long id;
    private Long vehicleId;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime dateTrigger;
}
