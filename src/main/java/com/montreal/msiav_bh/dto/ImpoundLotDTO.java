package com.montreal.msiav_bh.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpoundLotDTO {
	
    private LocalDateTime impoundArrivalDateTime;
    private LocalDateTime impoundDepartureDateTime;
    
}
