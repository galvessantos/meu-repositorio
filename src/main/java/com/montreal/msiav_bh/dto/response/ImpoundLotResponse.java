package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpoundLotResponse {
    
    @JsonProperty("impound_arrival_date_time")
    private LocalDateTime impoundArrivalDateTime;

    @JsonProperty("impound_departure_date_time")
    private LocalDateTime impoundDepartureDateTime;
}
