package com.montreal.msiav_bh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

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
