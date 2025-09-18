package com.montreal.msiav_bh.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.montreal.msiav_bh.dto.request.CollectedRequest;
import com.montreal.msiav_bh.dto.request.ImpoundLotRequest;
import com.montreal.msiav_bh.dto.request.LocationRequest;
import com.montreal.msiav_bh.enumerations.TypeHistoryEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class HistoryRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_vehicle")
    private Long idVehicle;

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
    private LocationRequest location;

    @JsonProperty("collected")
    private CollectedRequest collected;

    @JsonProperty("impound_lot")
    private ImpoundLotRequest impoundLot;
}