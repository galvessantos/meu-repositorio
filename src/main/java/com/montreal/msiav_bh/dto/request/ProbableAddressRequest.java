package com.montreal.msiav_bh.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProbableAddressRequest {
	
    private Long vehicleId;
    private AddressRequest address;
    
}
