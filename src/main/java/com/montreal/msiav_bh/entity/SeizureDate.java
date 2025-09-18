package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seizure_date", uniqueConstraints = {
    @UniqueConstraint(name = "unique_vehicle_seizure_date", columnNames = {"vehicle_id", "seizure_date"})
})
public class SeizureDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O veículo associado é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull(message = "A data de apreensão é obrigatória.")
    @Column(name = "seizure_date", nullable = false)
    private LocalDateTime seizureDate;

    @NotNull(message = "A data de criação é obrigatória.")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
