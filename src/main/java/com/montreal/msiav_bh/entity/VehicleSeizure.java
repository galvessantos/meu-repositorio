package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import com.montreal.oauth.domain.entity.UserInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "vehicle_seizure")
public class VehicleSeizure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @NotNull(message = "O usuário associado é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user; // Usuário associado à apreensão

    @NotNull(message = "O veículo associado é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull(message = "O endereço associado é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address; // Endereço associado à apreensão

    @NotNull(message = "A empresa associada é obrigatória.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Empresa associada à apreensão

    @NotNull(message = "A condição do veículo é obrigatória.")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_condition", nullable = false)
    private VehicleConditionEnum vehicleCondition;

    @NotNull(message = "A data da apreensão é obrigatória.")
    @Column(name = "seizure_date", nullable = false)
    private LocalDateTime seizureDate; // Data e hora da apreensão

    @NotNull(message = "A data de criação é obrigatória.")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Data de criação do registro

    @NotBlank(message = "A descrição é obrigatória.")
    @Column(name = "description", nullable = false, length = 500)
    private String description; // Descrição opcional

    @NotNull(message = "O status é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeizureStatusEnum status;
}
