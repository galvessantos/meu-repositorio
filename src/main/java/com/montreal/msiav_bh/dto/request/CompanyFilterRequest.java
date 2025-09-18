package com.montreal.msiav_bh.dto.request;

import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFilterRequest {
    private CompanyTypeEnum companyType;
    private String name;
    private String document;

}
