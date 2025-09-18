package com.montreal.msiav_bh.dto.request;

import java.util.Set;

import lombok.Data;

@Data
public class TriggerMultiVehicleplesRequest {
	
	private Set<Long> vehicleIds;
 
}
