package com.montreal.msiav_bh.dto;

import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SeizureNoticeDTO(
        String contractNumber,
        VehicleConditionEnum vehicleCondition,
        LocalDateTime seizureDateTime,
        Long seizureId,
        Long vehicleId
) {}
