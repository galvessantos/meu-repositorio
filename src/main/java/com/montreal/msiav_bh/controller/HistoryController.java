package com.montreal.msiav_bh.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.ErrorHandlerUtil;
import com.montreal.msiav_bh.dto.HistoryRequest;
import com.montreal.msiav_bh.dto.response.HistoryResponse;
import com.montreal.msiav_bh.service.HistoryService;
import com.montreal.oauth.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
@Tag(name = "Historico", description = "Operações para gerenciamento de históricos")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class HistoryController  {

    private final HistoryService historyService;

    @Operation(summary = "Listar histórico por ID do veículo")
    @GetMapping("/by-vehicle")
    public Page<HistoryResponse> listAllByIdVehicle(@RequestParam Long vehicleId, Pageable pageable) {
        return historyService.listAllByIdVehiclePage(vehicleId, pageable);
    }
    
    @Operation(summary = "Salvar novo histórico")
    @PostMapping
    public ResponseEntity<?> createHistory(@Valid @RequestBody HistoryRequest request) {
        try {
            log.info("Salvando histórico para veículo {}", request.getIdVehicle());
            HistoryResponse response = historyService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao salvar histórico", e);
            return ErrorHandlerUtil.handleError("Erro ao salvar histórico.", e);
        }
    }
    
    @Operation(summary = "Atualizar histórico existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHistory(@PathVariable Long id, @Valid @RequestBody HistoryRequest request) {
        try {
            log.info("Atualizando histórico com ID {}", id);
            HistoryResponse response = historyService.update(request, id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao atualizar histórico com ID {}", id, e);
            return ErrorHandlerUtil.handleError("Erro ao atualizar histórico.", e);
        }
    }
    
    @Operation(summary = "Buscar histórico por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Iniciando busca do histórico com ID={}", id);
        try {
            if (id == null || id <= 0) {
                log.warn("Parâmetro inválido: ID nulo ou menor que zero.");
                throw new InvalidParameterException("O parâmetro 'id' deve ser informado e maior que zero.");
            }

            HistoryResponse response = historyService.getById(id);
            log.info("Histórico com ID={} encontrado com sucesso.", id);
            return ResponseEntity.ok(response);

        } catch (InvalidParameterException e) {
            log.warn("Erro de validação: {}", e.getMessage());
            return ErrorHandlerUtil.handleError("Parâmetro inválido.", e);

        } catch (NotFoundException e) {
            log.warn("Histórico não encontrado: {}", e.getMessage());
            return ErrorHandlerUtil.handleError("Histórico não encontrado.", e);

        } catch (BusinessException e) {
            log.error("Erro de negócio ao buscar histórico ID={}: {}", id, e.getMessage(), e);
            return ErrorHandlerUtil.handleError("Erro de negócio ao buscar histórico.", e);

        } catch (Exception e) {
            log.error("Erro inesperado ao buscar histórico com ID={}", id, e);
            return ErrorHandlerUtil.handleError("Erro ao buscar histórico.", e);
        }
    }

}
