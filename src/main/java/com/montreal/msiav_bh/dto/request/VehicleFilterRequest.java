package com.montreal.msiav_bh.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFilterRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
