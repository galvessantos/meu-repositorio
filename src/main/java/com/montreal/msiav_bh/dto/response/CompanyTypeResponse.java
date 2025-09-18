package com.montreal.msiav_bh.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTypeResponse {

    @Schema(description = "Código do tipo da empresa")
    private String code;

    @Schema(description = "Descrição do tipo da empresa")
    private String description;

}
