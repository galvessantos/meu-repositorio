package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.context.CacheUpdateContext;
import com.montreal.msiav_bh.dto.PageDTO;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO;
import com.montreal.msiav_bh.mapper.VehicleInquiryMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleApiService {

    private final ApiQueryService apiQueryService;
    private final VehicleInquiryMapper vehicleInquiryMapper;
    private final VehicleCacheService vehicleCacheService;
    private final VehicleDataFetchService vehicleDataFetchService;

    private static final String CIRCUIT_BREAKER_NAME = "vehicle-api";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackToDatabase")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public PageDTO<VehicleDTO> getVehiclesWithFallback(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir) {

        log.info("==== INICIANDO BUSCA DE VEÍCULOS (DATABASE-FIRST) ====");

        try {
            VehicleCacheService.CacheStatus cacheStatus = vehicleCacheService.getCacheStatus();

            PageDTO<VehicleDTO> databaseResult = getFromDatabaseDecrypted(
                    dataInicio, dataFim, credor, contrato, protocolo, cpf,
                    uf, cidade, modelo, placa, etapaAtual, statusApreensao,
                    page, size, sortBy, sortDir
            );

            boolean shouldFetchFromApi = shouldFetchFromApiBasedOnCacheState(
                    cacheStatus, databaseResult, dataInicio, dataFim
            );

            if (shouldFetchFromApi) {
                log.warn("Condições detectadas para busca da API - cache vazio/desatualizado ou sem resultados");
                return fetchFromApiAndUpdateCache(dataInicio, dataFim, credor, contrato,
                        protocolo, cpf, uf, cidade, modelo, placa, etapaAtual,
                        statusApreensao, page, size, sortBy, sortDir);
            }

            if (!cacheStatus.isValid() && cacheStatus.getTotalRecords() > 0) {
                log.info("Cache desatualizado - iniciando atualização em background");
                scheduleBackgroundCacheUpdate(dataInicio, dataFim);
            }

            log.info("Retornando {} registros do PostgreSQL descriptografados", databaseResult.totalElements());
            return databaseResult;

        } catch (Exception e) {
            log.error("Erro ao buscar do banco - tentando API como fallback: {}", e.getMessage());
            throw e;
        }
    }

    private boolean shouldFetchFromApiBasedOnCacheState(
            VehicleCacheService.CacheStatus cacheStatus,
            PageDTO<VehicleDTO> databaseResult,
            LocalDate dataInicio,
            LocalDate dataFim) {

        if (cacheStatus.getTotalRecords() == 0) {
            log.info("Cache vazio - buscando da API");
            return true;
        }

        if (cacheStatus.getMinutesSinceLastSync() > 60 && databaseResult.content().isEmpty()) {
            log.info("Cache desatualizado ({}min) e sem resultados - buscando da API",
                    cacheStatus.getMinutesSinceLastSync());
            return true;
        }

        if (dataInicio != null && dataInicio.isAfter(LocalDate.now().minusDays(1)) &&
                databaseResult.content().isEmpty()) {
            log.info("Busca por período recente sem resultados - verificando API");
            return true;
        }

        return false;
    }


    private void scheduleBackgroundCacheUpdate(LocalDate dataInicio, LocalDate dataFim) {
        CompletableFuture.runAsync(() -> {
                    try {
                        updateCacheInBackgroundEncrypted(dataInicio, dataFim);
                    } catch (Exception e) {
                        log.error("Erro na atualização em background: {}", e.getMessage());
                    }
                }).orTimeout(5, TimeUnit.MINUTES)
                .exceptionally(throwable -> {
                    log.error("Timeout ou erro na atualização em background: {}", throwable.getMessage());
                    return null;
                });
    }

    private PageDTO<VehicleDTO> getFromDatabaseDecrypted(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir) {

        log.debug("Consultando PostgreSQL e descriptografando para frontend");

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<VehicleDTO> result = vehicleCacheService.getFromCache(
                dataInicio, dataFim, credor, contrato, protocolo, cpf,
                uf, cidade, modelo, placa, etapaAtual, statusApreensao, pageable
        );

        return PageDTO.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    private PageDTO<VehicleDTO> fetchFromApiAndUpdateCache(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir) {

        log.info("Buscando dados da API externa, salvando criptografados no cache e retornando descriptografados");

        try {
            LocalDate searchStart = dataInicio != null ? dataInicio : LocalDate.now().minusDays(30);
            LocalDate searchEnd = dataFim != null ? dataFim : LocalDate.now();

            CompletableFuture<PageDTO<VehicleDTO>> future = CompletableFuture.supplyAsync(() -> {
                // Usar o novo serviço otimizado que já busca dados completos
                List<VehicleDTO> vehiclesEncrypted = vehicleDataFetchService.fetchCompleteVehicleData(searchStart, searchEnd);
                log.info("Dados completos obtidos e criptografados: {} veículos", vehiclesEncrypted.size());

                if (!vehiclesEncrypted.isEmpty()) {
                    CacheUpdateContext context = CacheUpdateContext.filteredSearch(
                            searchStart, searchEnd, credor, contrato, protocolo, cpf
                    );
                    vehicleCacheService.updateCacheThreadSafe(vehiclesEncrypted, context);
                    log.info("Cache atualizado com dados criptografados");

                    return getFromDatabaseDecrypted(dataInicio, dataFim, credor, contrato, protocolo,
                            cpf, uf, cidade, modelo, placa, etapaAtual, statusApreensao,
                            page, size, sortBy, sortDir);
                }

                log.warn("API retornou lista vazia");
                return PageDTO.of(List.<VehicleDTO>of(), page, size, 0);
            });

            return future.get(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Erro ao buscar da API: {}", e.getMessage());
            throw new RuntimeException("Falha ao buscar dados da API", e);
        }
    }

    private void updateCacheInBackgroundEncrypted(LocalDate dataInicio, LocalDate dataFim) {
        try {
            log.debug("Iniciando atualização de cache em background com criptografia");

            LocalDate searchStart = dataInicio != null ? dataInicio : LocalDate.now().minusDays(30);
            LocalDate searchEnd = dataFim != null ? dataFim : LocalDate.now();

            List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                    apiQueryService.searchByPeriod(searchStart, searchEnd);

            List<VehicleDTO> vehicles = vehicleInquiryMapper.mapToVeiculoDTO(notifications);

            if (!vehicles.isEmpty()) {
                CacheUpdateContext context = CacheUpdateContext.scheduledRefresh(searchStart, searchEnd);
                vehicleCacheService.updateCacheThreadSafe(vehicles, context); // ✅ Usando thread-safe
                log.info("Cache atualizado em background com {} registros criptografados", vehicles.size());
            } else {
                log.info("Atualização em background: API retornou vazio");
            }

        } catch (Exception e) {
            log.error("Erro na atualização em background do cache: {}", e.getMessage());
        }
    }

    private PageDTO<VehicleDTO> fetchFromApiDirectDecrypted(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir) {

        log.info("Buscando diretamente da API (modo emergência) - dados descriptografados para frontend");

        try {
            LocalDate searchStart = dataInicio != null ? dataInicio : LocalDate.now().minusDays(30);
            LocalDate searchEnd = dataFim != null ? dataFim : LocalDate.now();

            List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                    apiQueryService.searchByPeriod(searchStart, searchEnd);

            List<VehicleDTO> vehicles = vehicleInquiryMapper.mapToVeiculoDTOForResponse(notifications);

            vehicles = applyLocalFilters(vehicles, credor, contrato, protocolo,
                    cpf, uf, cidade, modelo, placa, etapaAtual, statusApreensao);

            vehicles = sortVehicles(vehicles, sortBy, sortDir);

            return paginateVehicles(vehicles, page, size);

        } catch (Exception e) {
            log.error("Falha crítica ao buscar da API: {}", e.getMessage());
            return PageDTO.of(List.of(), page, size, 0);
        }
    }

    public PageDTO<VehicleDTO> fallbackToDatabase(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir, Throwable throwable) {

        log.warn("Circuit breaker ativado - usando apenas dados do PostgreSQL. Erro: {}",
                throwable.getMessage());

        try {
            PageDTO<VehicleDTO> result = getFromDatabaseDecrypted(dataInicio, dataFim, credor, contrato, protocolo,
                    cpf, uf, cidade, modelo, placa, etapaAtual, statusApreensao,
                    page, size, sortBy, sortDir);

            if (result.content().isEmpty() && dataInicio != null) {
                log.info("Fallback: expandindo período de busca no cache");
                LocalDate expandedStart = dataInicio.minusDays(30);
                LocalDate expandedEnd = dataFim != null ? dataFim.plusDays(7) : LocalDate.now().plusDays(7);

                result = getFromDatabaseDecrypted(expandedStart, expandedEnd, credor, contrato, protocolo,
                        cpf, uf, cidade, modelo, placa, etapaAtual, statusApreensao,
                        page, size, sortBy, sortDir);
            }

            return result;
        } catch (Exception e) {
            log.error("Falha ao buscar do banco no fallback: {}", e.getMessage());
            return PageDTO.of(List.of(), page, size, 0);
        }
    }

    private List<VehicleDTO> applyLocalFilters(List<VehicleDTO> vehicles,
                                               String credor, String contrato, String protocolo, String cpf,
                                               String uf, String cidade, String modelo, String placa,
                                               String etapaAtual, String statusApreensao) {

        return vehicles.stream()
                .filter(v -> credor == null || (v.credor() != null && v.credor().contains(credor)))
                .filter(v -> contrato == null || (v.contrato() != null && v.contrato().equals(contrato)))
                .filter(v -> protocolo == null || (v.protocolo() != null && v.protocolo().equals(protocolo)))
                .filter(v -> cpf == null || (v.cpfDevedor() != null && v.cpfDevedor().equals(cpf)))
                .filter(v -> uf == null || (v.uf() != null && v.uf().equals(uf)))
                .filter(v -> cidade == null || (v.cidade() != null && v.cidade().contains(cidade)))
                .filter(v -> modelo == null || (v.modelo() != null && v.modelo().contains(modelo)))
                .filter(v -> placa == null || (v.placa() != null && v.placa().equals(placa)))
                .filter(v -> etapaAtual == null || (v.etapaAtual() != null && v.etapaAtual().equals(etapaAtual)))
                .filter(v -> statusApreensao == null || (v.statusApreensao() != null && v.statusApreensao().equals(statusApreensao)))
                .toList();
    }

    private List<VehicleDTO> sortVehicles(List<VehicleDTO> vehicles, String sortBy, String sortDir) {
        var comparator = switch (sortBy.toLowerCase()) {
            case "credor" -> java.util.Comparator.comparing(VehicleDTO::credor,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "datapedido" -> java.util.Comparator.comparing(VehicleDTO::dataPedido,
                    java.util.Comparator.nullsLast(LocalDate::compareTo));
            case "placa" -> java.util.Comparator.comparing(VehicleDTO::placa,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> java.util.Comparator.comparing(VehicleDTO::protocolo,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        };

        return vehicles.stream()
                .sorted("desc".equalsIgnoreCase(sortDir) ? comparator.reversed() : comparator)
                .toList();
    }

    private PageDTO<VehicleDTO> paginateVehicles(List<VehicleDTO> vehicles, int page, int size) {
        int total = vehicles.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        return start >= total
                ? PageDTO.of(List.of(), page, size, total)
                : PageDTO.of(vehicles.subList(start, end), page, size, total);
    }

    public void forceRefreshFromApi() {
        log.info("Forçando atualização manual do cache via API");

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                    apiQueryService.searchByPeriod(startDate, endDate);

            List<VehicleDTO> vehicles = vehicleInquiryMapper.mapToVeiculoDTO(notifications);

            if (!vehicles.isEmpty()) {
                CacheUpdateContext context = CacheUpdateContext.fullRefresh();
                vehicleCacheService.updateCacheThreadSafe(vehicles, context);
                log.info("Cache forçadamente atualizado - {} registros", vehicles.size());
            }

        } catch (Exception e) {
            log.error("Erro ao forçar atualização: {}", e.getMessage());
            throw new RuntimeException("Falha ao forçar atualização do cache", e);
        }
    }

}