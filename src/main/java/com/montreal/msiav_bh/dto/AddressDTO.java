package com.montreal.msiav_bh.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
	
    private Long id;
    private String postalCode;
    private String street;
    private String number;
    private String neighborhood;
    private String complement;
    private String state;
    private String city;
    private String note;
    
}
