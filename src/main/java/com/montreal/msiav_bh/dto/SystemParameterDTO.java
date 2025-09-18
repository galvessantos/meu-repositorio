package com.montreal.msiav_bh.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameterDTO {
	
    private Long id;
    private String system;
    private String parameter;
    private String value;
    
}
