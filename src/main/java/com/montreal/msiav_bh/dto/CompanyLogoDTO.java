package com.montreal.msiav_bh.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLogoDTO {
	
    private Long id;
    private Long companyIdMongo;
    private String companyImage;
    
}
