package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.TypeHistoryEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_vehicle"))
    private Vehicle vehicle;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate; // Placa do veículo

    @Column(nullable = false)
    private String model; // Modelo do veículo

    @Column(name = "creditor_name", nullable = false)
    private String creditorName; // Nome do credor relacionado ao veículo

    @Column(name = "contract_number", nullable = false)
    private String contractNumber; // Número do contrato associado ao veículo

    @NotNull(message = "A data e hora de criação do histórico são obrigatórias.")
    @Column(name = "creation_date_time", nullable = false)
    private LocalDateTime creationDateTime; // Data e hora de criação do histórico

    @Enumerated(EnumType.STRING)
    @Column(name = "type_history", nullable = false)
    private TypeHistoryEnum typeHistory; // Tipo do histórico, representado por um enum
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "collected_id", referencedColumnName = "id")
    private Collected collected;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "impound_lot_id", referencedColumnName = "id")
    private ImpoundLot impoundLot;

}
