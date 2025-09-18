package com.montreal.msiav_bh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_address", 
       uniqueConstraints = @UniqueConstraint(name = "unique_vehicle_address", columnNames = {"vehicle_id", "address_id"}))
public class VehicleAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O veículo não pode estar vazio.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vehicle_address_vehicle"))
    private Vehicle vehicle;

    @NotNull(message = "O endereço não pode estar vazio.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vehicle_address_address"))
    private Address address;

    @NotNull(message = "A data de associação é obrigatória.")
    @Column(name = "associated_date", nullable = false)
    private LocalDateTime associatedDate;
}
