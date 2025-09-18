package com.montreal.msiav_bh.dto.response;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import com.montreal.oauth.domain.dto.response.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSeizureResponse {

    private Long id;

//    @JsonIgnoreProperties("roles")
    private UserResponse user;

    private VehicleResponse vehicle;

    private AddressResponse address;

    private CompanyResponse company;

    private VehicleConditionEnum vehicleCondition;
    private LocalDateTime seizureDate;
    private LocalDateTime createdAt;
    private String description;
    private SeizureStatusEnum status;
}

