package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.PageDTO;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ContractDetails;
import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.entity.Contract;
import com.montreal.msiav_bh.service.ApiQueryService;
import com.montreal.msiav_bh.service.ContractPersistenceService;
import com.montreal.msiav_bh.service.VehicleApiService;
import com.montreal.msiav_bh.service.VehicleCacheService;
import com.montreal.msiav_bh.utils.exceptions.ValidationMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vehicle")
@Tag(name = "Veiculos", description = "Veiculos - Database-First Strategy")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class VehicleController {

    private final VehicleApiService vehicleApiService;
    private final VehicleCacheService vehicleCacheService;
    private final ContractPersistenceService persistenceService;
    @Autowired
    private ApiQueryService apiQueryService;

    @GetMapping
    @Operation(summary = "Buscar veículos (Database-First)")
    public ResponseEntity<?> buscarVeiculos(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) String credor,
            @RequestParam(required = false) String contrato,
            @RequestParam(required = false) String protocolo,
            @RequestParam(required = false) @Pattern(regexp = "\\d{11,14}", message = ValidationMessages.CPF_INVALIDO) String cpf,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String etapaAtual,
            @RequestParam(required = false) String statusApreensao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "protocolo") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletResponse httpResponse
    ) {
        log.info("Requisição recebida - Estratégia: DATABASE-FIRST");

        if (dataInicio == null && dataFim == null) {
            log.info("Nenhum período especificado - retornando todos os registros");
        } else {

            if (dataInicio == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "When providing an end date (dataFim), you must also provide a start date (dataInicio)",
                        "source", "validation"
                ));
            }
            if (dataFim == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "When providing a start date (dataInicio), you must also provide an end date (dataFim)",
                        "source", "validation"
                ));
            }
            if (dataFim.isBefore(dataInicio)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "End date (dataFim) cannot be before start date (dataInicio)",
                        "source", "validation"
                ));
            }
        }

        try {
            PageDTO<VehicleDTO> resultado = vehicleApiService.getVehiclesWithFallback(
                    dataInicio, dataFim, credor, contrato, protocolo, cpf, uf, cidade,
                    modelo, placa, etapaAtual, statusApreensao, page, size, sortBy, sortDir
            );

            httpResponse.setHeader("X-Data-Strategy", "Database-First");
            httpResponse.setHeader("X-Data-Source", "PostgreSQL");

            VehicleCacheService.CacheStatus cacheStatus = vehicleCacheService.getCacheStatus();
            httpResponse.setHeader("X-Cache-Status", cacheStatus.isValid() ? "Valid" : "Outdated");
            httpResponse.setHeader("X-Cache-Age-Minutes", String.valueOf(cacheStatus.getMinutesSinceLastSync()));

            Map<String, Object> response = new HashMap<>();
            response.put("data", resultado);
            response.put("metadata", Map.of(
                    "source", "PostgreSQL",
                    "strategy", "Database-First",
                    "cacheValid", cacheStatus.isValid(),
                    "cacheAgeMinutes", cacheStatus.getMinutesSinceLastSync(),
                    "totalRecordsInCache", cacheStatus.getTotalRecords()
            ));

            log.info("Resposta enviada com {} registros", resultado.content().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao processar requisição: {}", e.getMessage(), e);

            httpResponse.setHeader("X-Data-Strategy", "Database-First");
            httpResponse.setHeader("X-Data-Source", "Error");

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao processar requisição",
                    "message", e.getMessage(),
                    "source", "internal"
            ));
        }
    }


    @GetMapping("/health")
    @Operation(summary = "Verificar saúde da API")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "strategy", "Database-First",
                "message", "API operacional com estratégia Database-First"
        ));
    }

    @DeleteMapping("/cache/invalidate")
    @Operation(summary = "Invalidar todo o cache (usar com cuidado)")
    public ResponseEntity<Map<String, Object>> invalidateCache(
            @RequestHeader(value = "X-Confirm-Action", required = false) String confirmAction) {

        if (!"CONFIRM_INVALIDATE".equals(confirmAction)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Ação não confirmada. Adicione o header 'X-Confirm-Action: CONFIRM_INVALIDATE'",
                    "warning", "Esta operação remove TODOS os dados do cache"
            ));
        }

        try {
            log.warn("INVALIDAÇÃO DE CACHE SOLICITADA - Removendo todos os dados do cache");
            long recordsBeforeInvalidation = vehicleCacheService.getCacheStatus().getTotalRecords();

            vehicleCacheService.invalidateCache();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cache invalidado com sucesso",
                    "recordsRemoved", recordsBeforeInvalidation,
                    "warning", "Próxima consulta forçará atualização via API"
            ));
        } catch (Exception e) {
            log.error("Falha ao invalidar cache", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Falha ao invalidar o cache",
                    "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/cache/enrich")
    @Operation(summary = "Enriquecer dados incompletos do cache")
    public ResponseEntity<Map<String, Object>> enrichCacheData() {
        try {
            log.info("Enriquecimento de cache solicitado via API");
            
            // Buscar veículos com dados incompletos
            var incompleteVehicles = vehicleCacheService.getVehiclesWithIncompleteData();
            
            if (incompleteVehicles.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Nenhum veículo com dados incompletos encontrado",
                        "vehiclesProcessed", 0
                ));
            }
            
            // Iniciar enriquecimento assíncrono
            vehicleCacheService.enrichIncompleteVehicles();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Enriquecimento iniciado em background",
                    "vehiclesToProcess", incompleteVehicles.size(),
                    "info", "O processo está rodando em background. Verifique os logs para acompanhar o progresso."
            ));
            
        } catch (Exception e) {
            log.error("Falha ao iniciar enriquecimento do cache", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Falha ao iniciar enriquecimento",
                    "error", e.getMessage()
            ));
        }
    }
}