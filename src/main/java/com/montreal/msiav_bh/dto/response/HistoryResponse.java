package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.montreal.msiav_bh.enumerations.TypeHistoryEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("model")
    private String model;

    @JsonProperty("creditor_name")
    private String creditorName;

    @JsonProperty("contract_number")
    private String contractNumber;

    @JsonProperty("creation_date_time")
    private LocalDateTime creationDateTime;

    @JsonProperty("type_history")
    private TypeHistoryEnum typeHistory;

    @JsonProperty("location")
    private LocationResponse location;

    @JsonProperty("collected")
    private CollectedResponse collected;

    @JsonProperty("impound_lot")
    private ImpoundLotResponse impoundLot;
}
