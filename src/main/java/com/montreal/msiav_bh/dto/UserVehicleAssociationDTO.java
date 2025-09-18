package com.montreal.msiav_bh.dto;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVehicleAssociationDTO {
	
    private Long id;
    private Long userId;
    private Long vehicleId;
    private Long associatedById;
    private LocalDateTime createdAt;
    private CompanyTypeEnum companyTypeEnum;
    
}
