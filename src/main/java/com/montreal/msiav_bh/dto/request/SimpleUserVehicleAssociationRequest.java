package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SimpleUserVehicleAssociationRequest {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("vehicle_id")
    private Long vehicleId;
}
