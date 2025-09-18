package com.montreal.msiav_bh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProbableAddressResponse {
	
    private Long id;
    private Long vehicleId;
    private AddressResponse address;
    
}
