package com.montreal.msiav_bh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Id da data de apreensão não pode ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_seizure_id", nullable = false)
    private VehicleSeizure vehicleSeizure;

    @NotNull(message = "Número do mandato não pode ser nulo")
    @Size(min = 1, max = 50, message = "Número do mandato deve ter entre 1 e 50 caracteres")
    @Column(name = "mandate_number", nullable = false, length = 50)
    private String mandateNumber;

    @NotNull(message = "Data do mandato não pode ser nulo")
    @Column(name = "mandate_date", nullable = false)
    private LocalDate mandateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "witness_id")
    private Witnesses witness; // Testemunha associada

    @NotNull(message = "Id do contrato não pode ser nulo")
    @Column(name = "contract_id", nullable = false)
    private String contract; // ID do contrato (String)

    @NotNull(message = "Número do contrato não pode ser nulo")
    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber; // Número do contrato (String)

    @NotNull(message = "Valor da dívida não pode ser nulo")
    @Column(name = "debt_value", nullable = false)
    private BigDecimal debtValue;

    @NotNull(message = "Id do guincho não pode ser nulo")
    @Column(name = "tow_truck_id", nullable = false)
    private String towTruck; // ID do guincho (String)

    @NotNull(message = "Id da notificação não pode ser nulo")
    @Column(name = "notification_id", nullable = false)
    private String notification; // ID da notificação (String)

    @NotNull(message = "Id do aviso de recebimento da notificação não pode ser nulo")
    @Column(name = "ar_notification_id", nullable = false)
    private String arNotification; // ID do aviso de recebimento da notificação (String)
}
