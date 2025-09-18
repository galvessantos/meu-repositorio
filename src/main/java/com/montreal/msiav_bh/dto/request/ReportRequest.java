package com.montreal.msiav_bh.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotNull(message = "Id da data de apreensão não pode ser nulo")
    @JsonProperty("seizureDateId")
    private Long vehicleSeizureId;

    @NotNull(message = "Número do mandato não pode ser nulo")
    @Size(min = 1, max = 50, message = "Número do mandato deve ter entre 1 e 50 caracteres")
    private String mandateNumber;

    @NotNull(message = "Data do mandato não pode ser nulo")
    private LocalDate mandateDate;

    private Long witnessId;

    @NotNull(message = "Id do contrato não pode ser nulo")
    @JsonProperty("contractId")
    @JsonAlias("contract")
    private String contract;

    @NotNull(message = "Número do contrato não pode ser nulo")
    private String contractNumber;

    @NotNull(message = "Valor da dívida não pode ser nulo")
    private BigDecimal debtValue;

    @NotNull(message = "Id do guincho não pode ser nulo")
    @JsonProperty("towTruckId")
    @JsonAlias("towTruck")
    private String towTruck;

    @NotNull(message = "Id da notificação não pode ser nulo")
    @JsonProperty("notificationId")
    @JsonAlias("notification")
    private String notification;

    @NotNull(message = "Id do aviso de recebimento da notificação não pode ser nulo")
    @JsonProperty("arNotificationId")
    @JsonAlias("arNotification")
    private String arNotification;
}
