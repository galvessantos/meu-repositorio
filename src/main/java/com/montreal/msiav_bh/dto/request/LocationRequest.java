package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequest {

	 @JsonProperty("vehicle_found")
	 private Boolean vehicleFound;
	
	 @JsonProperty("note")
	 private String note;
}

