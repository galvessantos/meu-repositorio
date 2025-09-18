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
public class CollectedDTO {
	
    private Boolean vehicleFound;
    private String note;
    private LocalDateTime collectionDateTime;
    
}
