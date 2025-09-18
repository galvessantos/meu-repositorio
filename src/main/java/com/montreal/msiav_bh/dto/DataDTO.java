package com.montreal.msiav_bh.dto;

import java.util.List;

public record DataDTO(
        List<AgencyDTO> orgaoEscob,
        List<AgencyDTO> orgaoGuincho,
        List<AgencyDTO> orgaoDespachante,
        List<AgencyDTO> orgaoLeilao,
        List<AgencyDTO> orgaoLocalizador,
        List<AgencyDTO> orgaoPatio,
        List<NotificationDTO> notificacaoEletronica,
        List<NotificationDTO> notificacaoViaAr,
        CreditorDTO credor,
        List<DebtorDTO> devedores,
        List<GuarantorDTO> garantidores,
        List<VehicleDTO> veiculos,
        ContractDTO contrato,
        NotaryDTO serventia,
        DetranDTO detran
) {}