package com.montreal.msiav_bh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WitnessesResponse {
    
    private Long id;
    private String name;
    private String rg;
    private String cpf;
}
