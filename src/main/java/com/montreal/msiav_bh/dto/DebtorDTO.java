package com.montreal.msiav_bh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtorDTO {
	
    private Long id;
    private String name;
    private String cpfCnpj;
    private AddressDTO address;
    
}
