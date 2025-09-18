package com.montreal.msiav_bh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
	
    private Boolean vehicleFound;
    private String note;
    private AddressDTO address;
}
