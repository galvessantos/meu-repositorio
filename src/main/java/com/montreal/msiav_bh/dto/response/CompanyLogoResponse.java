package com.montreal.msiav_bh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLogoResponse {
    
    private Long id;
    private Long companyId;
    private String companyImage;
    private String message;
}
