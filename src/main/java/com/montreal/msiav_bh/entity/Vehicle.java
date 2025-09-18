package com.montreal.msiav_bh.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.FuelTypeEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.enumerations.VehicleStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle",
        uniqueConstraints = {
                @UniqueConstraint(name = "licensePlate_contractNumber_idx", columnNames = {"license_plate", "contract_number"}),
                @UniqueConstraint(name = "unique_renavam", columnNames = {"renavam"}),
                @UniqueConstraint(name = "unique_chassi", columnNames = {"chassi"})
        })
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, length = 15)
    private String licensePlate;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "manufacture_year", nullable = true)
    private Integer manufactureYear;

    @Column(name = "model_year", nullable = true)
    private Integer modelYear;

    @Column(name = "registration_state", nullable = true, length = 2)
    private String registrationState;

    @Column(name = "creditor_name", nullable = true, length = 255)
    private String creditorName;

    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "renavam", nullable = true, length = 9)
    private String renavam;

    @Column(name = "chassi", nullable = true, length = 17)
    private String chassi;

    @Column(name = "color", nullable = true, length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = true, length = 20)
    private FuelTypeEnum fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = true, length = 50)
    private VehicleStageEnum stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private VehicleStatusEnum status;

    @PastOrPresent(message = "A data da solicitação não pode estar no futuro.")
    @Column(name = "request_date")
    private LocalDate requestDate;

    @PastOrPresent(message = "A data de apreensão do veículo não pode estar no futuro.")
    @Column(name = "seizure_date_time")
    private LocalDateTime seizureDateTime;

    @PastOrPresent(message = "A data da última movimentação não pode estar no futuro.")
    @Column(name = "last_movement_date")
    private LocalDateTime lastMovementDate;

    @FutureOrPresent(message = "A data de apreensão agendada deve estar no futuro ou presente.")
    @Column(name = "scheduled_seizure_date")
    private LocalDateTime scheduledSeizureDate;
}