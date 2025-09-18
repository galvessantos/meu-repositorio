package com.montreal.msiav_bh.dto.response;

import com.montreal.msiav_bh.enumerations.FuelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    
    private Long id;
    private String licensePlate;
    private Long vehicleSeizureId;
    private String model;
    private String registrationState;
    private String creditorName;
    private String contractNumber;
    private String renavam;
    private String chassi;
    private FuelTypeEnum fuelType;
    private Integer manufactureYear;
    private Integer modelYear;
    private String stage;
    private String status;
    private LocalDate requestDate;
    private LocalDateTime seizureDateTime;
    private LocalDateTime lastMovementDate;
    private LocalDateTime seizureScheduledDate;
    private CompanyResponse dentran;
}
