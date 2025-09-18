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
public class SeizureDateDTO {
	
    private Long id;
    private Long vehicleId;
    private LocalDateTime seizureDate;
    private LocalDateTime createdAt;
    
}
