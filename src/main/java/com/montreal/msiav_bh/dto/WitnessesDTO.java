package com.montreal.msiav_bh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WitnessesDTO {
	
    private Long id;
    private String name;
    private String rg;
    private String cpf;
    
}
