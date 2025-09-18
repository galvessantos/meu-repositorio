package com.montreal.msiav_bh.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record QueryResultDTO(
        Long id,
        String statusApreensao,
        LocalDateTime dataHoraApreensao,
        LocalDateTime dataUltimaMovimentacao,
        AddressDTO address,
        String etapaAtual,
        LocalDateTime agendamentoApreensao
) {}
