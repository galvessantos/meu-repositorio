package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.entity.QueryResult;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.repository.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class QueryResultService {

    private final QueryResultRepository queryResultRepository;
    private final VehicleCacheRepository vehicleCacheRepository;
    private final AddressRepository addressRepository;


    @Transactional
    public QueryResult getQueryResult(Long vehicleID) {
        VehicleCache cache = vehicleCacheRepository.findById(vehicleID).orElseThrow(() ->
                new IllegalArgumentException("Vehicle não encontrado")
        );

        QueryResult queryResult = queryResultRepository.findByVehicleId(vehicleID)
                .orElse(new QueryResult());

        queryResult.setVehicle(cache);


        if (queryResult.getStatusApreensao() == null) {
            queryResult.setStatusApreensao("Aguardando Agendamento da Diligência");
        }

        queryResult.setDataUltimaMovimentacao(cache.getUltimaMovimentacao());


        return queryResultRepository.save(queryResult);
    }

    @Transactional
    public QueryResult agendarApreensao(Long vehicleId, LocalDateTime dataHoraApreensao) {
        log.info("Agendando apreensão para vehicleId {} na data {}", vehicleId, dataHoraApreensao);

        // Busca o VehicleCache primeiro para garantir que existe
        VehicleCache vehicleCache = vehicleCacheRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle não encontrado para vehicleId " + vehicleId));

        QueryResult queryResult = queryResultRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("QueryResult não encontrado para vehicleId " + vehicleId));

        // Atualiza dataHora com o valor vindo do front
        queryResult.setAgendamentoApreensao(dataHoraApreensao);

        // Só muda o status se ainda não estiver agendado
        if (!"DILIGENCIA AGENDADA".equalsIgnoreCase(queryResult.getStatusApreensao())) {
            queryResult.setStatusApreensao("DILIGENCIA AGENDADA");

            // Atualiza também no VehicleCache
            vehicleCache.setStatusApreensao("DILIGENCIA AGENDADA");
            vehicleCacheRepository.save(vehicleCache);
            log.info("Status de apreensão atualizado no VehicleCache para vehicleId {}: {}", vehicleId, "DILIGENCIA AGENDADA");
        }

        QueryResult atualizado = queryResultRepository.save(queryResult);

        log.info("Apreensão agendada para vehicleId {} - status: {}, dataHoraApreensao: {}",
                vehicleId, atualizado.getStatusApreensao(), atualizado.getAgendamentoApreensao());

        return atualizado;
    }
}
