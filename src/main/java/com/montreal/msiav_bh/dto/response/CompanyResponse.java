package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;
import com.montreal.msiav_bh.enumerations.PhoneTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {
    
    private Long id;
    private String name;
    private String email;
    private String document;
    private String phoneDDD;
    private String phoneNumber;
    private PhoneTypeEnum phoneType;
    private AddressResponse address;
    private String nameResponsible;
    private CompanyTypeEnum companyType;
    private Boolean isActive;
    private String companyLogo;
    private LocalDateTime createdAt;
}
