package com.montreal.msiav_bh.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiDataDTO {
    private CreditorDTO credor;
    private List<DebtorDTO> devedores;
    private List<VehicleDTO> veiculos;
    private ContractDTO contrato;
    private NotaryDTO serventia;
    private List<AgencyDTO> orgaoLocalizador;
    private List<NotificationDTO> notificacaoEletronica;
    private List<NotificationDTO> notificacaoViaAr;
}