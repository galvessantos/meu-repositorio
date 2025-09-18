package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSeizureRequest {

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("vehicleId")
    private Long vehicleId;

    @JsonProperty("addressId")
    private Long addressId;

    @JsonProperty("companyId")
    private Long companyId;

    @JsonProperty("vehicleCondition")
    private VehicleConditionEnum vehicleCondition;

    @JsonProperty("seizureDate")
    private LocalDateTime seizureDate;

    @JsonProperty("description")
    private String description;

    @JsonProperty("seizureStatus")
    private SeizureStatusEnum seizureStatus;
}
