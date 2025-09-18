package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.request.ApreensaoRequest;
import com.montreal.msiav_bh.entity.QueryResult;
import com.montreal.msiav_bh.service.QueryResultService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/query-result")
@Tag(name = "Resultado da consulta", description = "Operações para resultado da consulta do modal detalhes de um veiculo")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class QueryResultController {

    private final QueryResultService queryResultService;


    @GetMapping("/{vehicleId}")
    public ResponseEntity<QueryResult> getQueryResult(@PathVariable @NotNull Long vehicleId) {
        log.info("Requisição recebida para buscar/atualizar QueryResult do veículo {}", vehicleId);

        QueryResult queryResult = queryResultService.getQueryResult(vehicleId);

        log.info("QueryResult retornado com sucesso para vehicleId {} - status: {}, dataHoraApreensao: {}",
                vehicleId, queryResult.getStatusApreensao(), queryResult.getDataHoraApreensao());

        return ResponseEntity.ok(queryResult);
    }

    @PutMapping("/{vehicleId}/apreensao")
    public ResponseEntity<QueryResult> agendarApreensao(
            @PathVariable Long vehicleId,
            @Valid @RequestBody ApreensaoRequest request) {

        log.info("Requisição para agendar apreensão do veículo {} na data {}", vehicleId, request.getDataHoraApreensao());
        QueryResult atualizado = queryResultService.agendarApreensao(vehicleId, request.getDataHoraApreensao());
        return ResponseEntity.ok(atualizado);
    }
}
