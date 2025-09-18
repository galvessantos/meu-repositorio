package com.montreal.msiav_bh.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.montreal.msiav_bh.context.CacheUpdateContext;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.mapper.VehicleCacheMapper;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCacheService {

    private final VehicleCacheRepository vehicleCacheRepository;
    private final VehicleCacheMapper vehicleCacheMapper;
    private final VehicleCacheCryptoService cryptoService;
    private VehicleCacheEnrichmentService enrichmentService; // Injeção circular evitada

    private final ReentrantLock cacheLock = new ReentrantLock();

    @Value("${vehicle.cache.expiry.minutes:10}")
    private int cacheExpiryMinutes;

    @Value("${vehicle.cache.retention.days:7}")
    private int cacheRetentionDays;

    private final Cache<String, Long> contratoToIdCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    private final Cache<String, Long> placaToIdCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    private final Cache<String, Long> protocoloToIdCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    @PostConstruct
    public void initializeCache() {
        refreshInMemoryCache();
    }
    
    // Setter para evitar dependência circular
    public void setEnrichmentService(VehicleCacheEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }
    
    public List<VehicleCache> getVehiclesWithIncompleteData() {
        return vehicleCacheRepository.findVehiclesWithIncompleteData();
    }
    
    public void enrichIncompleteVehicles() {
        if (enrichmentService != null) {
            enrichmentService.enrichIncompleteVehicles();
        } else {
            log.warn("Serviço de enriquecimento não está configurado");
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupInMemoryCache() {
        long contratoSize = contratoToIdCache.estimatedSize();
        long placaSize = placaToIdCache.estimatedSize();
        long protocoloSize = protocoloToIdCache.estimatedSize();

        log.info("Cache em memória - Contratos: {}, Placas: {}, Protocolos: {}",
                contratoSize, placaSize, protocoloSize);

        if (contratoSize > 8000 || placaSize > 8000 || protocoloSize > 8000) {
            log.info("Limpando cache em memória devido ao tamanho");
            contratoToIdCache.invalidateAll();
            placaToIdCache.invalidateAll();
            protocoloToIdCache.invalidateAll();
            refreshInMemoryCache();
        }
    }

    private void refreshInMemoryCache() {
        log.info("Inicializando cache em memória para otimizar comparações...");

        contratoToIdCache.invalidateAll();
        placaToIdCache.invalidateAll();
        protocoloToIdCache.invalidateAll();

        List<VehicleCache> recentVehicles = vehicleCacheRepository.findLatestCachedVehicles(
                org.springframework.data.domain.PageRequest.of(0, 5000)
        ).getContent();

        for (VehicleCache vehicle : recentVehicles) {
            try {
                String contratoDecrypted = cryptoService.decryptContrato(vehicle.getContrato());
                if (contratoDecrypted != null && !"N/A".equals(contratoDecrypted)) {
                    contratoToIdCache.put(contratoDecrypted.trim(), vehicle.getId());
                }

                String placaDecrypted = cryptoService.decryptPlaca(vehicle.getPlaca());
                if (placaDecrypted != null && !"N/A".equals(placaDecrypted)) {
                    placaToIdCache.put(placaDecrypted.trim().toUpperCase(), vehicle.getId());
                }

                if (vehicle.getProtocolo() != null && !"N/A".equals(vehicle.getProtocolo())) {
                    protocoloToIdCache.put(vehicle.getProtocolo().trim(), vehicle.getId());
                }
            } catch (Exception e) {
                log.trace("Erro ao descriptografar veículo ID {}: {}", vehicle.getId(), e.getMessage());
            }
        }

        log.info("Cache em memória inicializado: {} contratos, {} placas, {} protocolos",
                contratoToIdCache.estimatedSize(), placaToIdCache.estimatedSize(), protocoloToIdCache.estimatedSize());
    }

    public void updateCacheThreadSafe(List<VehicleDTO> vehicles, CacheUpdateContext context) {
        cacheLock.lock();
        try {
            log.info("Atualizando cache de forma thread-safe com {} veículos. Contexto: {}",
                    vehicles.size(), context);

            cleanDuplicates();
            doUpdateCache(vehicles, context);

            try {
                cleanOldCache();
            } catch (Exception e) {
                log.warn("Erro na limpeza do cache (não crítico): {}", e.getMessage());
            }

        } finally {
            cacheLock.unlock();
        }
    }

    @Transactional
    private void doUpdateCache(List<VehicleDTO> vehicles, CacheUpdateContext context) {
        log.info("Atualizando cache do PostgreSQL com {} veículos. Contexto: {}", vehicles.size(), context);

        try {
            LocalDateTime syncDate = LocalDateTime.now();

            if (context.isFullRefresh()) {
                handleFullRefresh(vehicles, syncDate, context);
            } else {
                handleIncrementalUpdate(vehicles, syncDate, context);
            }

            log.info("Cache do PostgreSQL atualizado com sucesso (dados sensíveis criptografados)");
        } catch (Exception e) {
            log.error("Erro ao atualizar cache do PostgreSQL", e);
            throw new RuntimeException("Falha ao atualizar cache", e);
        }
    }

    @Transactional
    public void updateCache(List<VehicleDTO> vehicles, CacheUpdateContext context) {
        updateCacheThreadSafe(vehicles, context);
    }

    private void handleFullRefresh(List<VehicleDTO> vehicles, LocalDateTime syncDate, CacheUpdateContext context) {
        log.debug("Executando atualização completa do cache");

        if (vehicles.isEmpty() && !context.isHasFilters()) {
            long currentCount = vehicleCacheRepository.count();
            if (currentCount > 100) {
                log.warn("API retornou vazio mas cache tem {} registros - preservando dados atuais", currentCount);
                return;
            } else {
                log.warn("API retornou vazio e cache pequeno - limpando cache");
                vehicleCacheRepository.deleteAll();
                return;
            }
        }

        if (vehicles.isEmpty()) {
            log.info("API retornou vazio com filtros - preservando cache existente");
            return;
        }

        Set<String> activePlacasEncrypted = vehicles.stream()
                .map(VehicleDTO::placa)
                .filter(Objects::nonNull)
                .filter(placa -> !"N/A".equals(placa) && !placa.trim().isEmpty())
                .map(cryptoService::encryptPlaca)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!activePlacasEncrypted.isEmpty()) {
            int removedCount = vehicleCacheRepository.countByPlacaNotIn(activePlacasEncrypted);

            long totalCache = vehicleCacheRepository.count();
            if (removedCount > totalCache * 0.8) {
                log.warn("Tentativa de remover {} de {} registros ({}%) - preservando dados",
                        removedCount, totalCache, (removedCount * 100 / totalCache));
            } else {
                vehicleCacheRepository.deleteByPlacaNotIn(activePlacasEncrypted);
                log.info("Removidos {} veículos não mais presentes na API", removedCount);
            }
        }

        updateOrInsertVehicles(vehicles, syncDate);
    }

    private void handleIncrementalUpdate(List<VehicleDTO> vehicles, LocalDateTime syncDate, CacheUpdateContext context) {
        log.debug("Executando atualização incremental do cache");
        updateOrInsertVehicles(vehicles, syncDate);
        log.info("Atualização incremental completada para {} veículos", vehicles.size());
    }

    private void updateOrInsertVehicles(List<VehicleDTO> vehicles, LocalDateTime syncDate) {
        int updated = 0;
        int inserted = 0;
        int duplicateSkipped = 0;
        int noChangesFound = 0;

        log.info("=== PROCESSANDO {} VEÍCULOS DA API ===", vehicles.size());

        for (VehicleDTO dto : vehicles) {
            try {
                Optional<VehicleCache> existing = findExistingVehicleOptimized(dto);

                if (existing.isPresent()) {
                    VehicleCache existingEntity = existing.get();

                    if (hasDataChanges(existingEntity, dto)) {
                        VehicleCache updatedEntity = updateExistingVehicle(existingEntity, dto, syncDate);

                        if (updatedEntity.getContratoHash() == null || updatedEntity.getPlacaHash() == null) {
                            String contratoDecrypted = cryptoService.decryptContrato(dto.contrato());
                            String placaDecrypted = cryptoService.decryptPlaca(dto.placa());
                            updatedEntity.setContratoHash(generateHash(contratoDecrypted));
                            updatedEntity.setPlacaHash(generateHash(placaDecrypted));
                            if (contratoDecrypted != null && placaDecrypted != null) {
                                updatedEntity.setContratoPlacaHash(generateHash(contratoDecrypted + "|" + placaDecrypted));
                            }
                        }

                        vehicleCacheRepository.save(updatedEntity);
                        updated++;

                        updateInMemoryCache(dto, existingEntity.getId());

                        log.debug("✓ Veículo ATUALIZADO: contrato={}, placa={}",
                                maskSensitiveData(dto.contrato()), maskSensitiveData(dto.placa()));
                    } else {
                        existingEntity.setApiSyncDate(syncDate);
                        vehicleCacheRepository.save(existingEntity);
                        noChangesFound++;
                        log.trace("⚡ Veículo SEM MUDANÇAS: contrato={}, placa={}",
                                maskSensitiveData(dto.contrato()), maskSensitiveData(dto.placa()));
                    }
                } else {
                    VehicleCache newEntity = vehicleCacheMapper.toEntity(dto, syncDate);
                    String contratoDecrypted = cryptoService.decryptContrato(dto.contrato());
                    String placaDecrypted = cryptoService.decryptPlaca(dto.placa());
                    newEntity.setContratoHash(generateHash(contratoDecrypted));
                    newEntity.setPlacaHash(generateHash(placaDecrypted));
                    if (contratoDecrypted != null && placaDecrypted != null) {
                        newEntity.setContratoPlacaHash(generateHash(contratoDecrypted + "|" + placaDecrypted));
                    }

                    VehicleCache savedEntity = vehicleCacheRepository.save(newEntity);
                    inserted++;

                    updateInMemoryCache(dto, savedEntity.getId());

                    log.debug("➕ NOVO veículo inserido: contrato={}, placa={}",
                            maskSensitiveData(dto.contrato()), maskSensitiveData(dto.placa()));
                }
            } catch (Exception e) {
                if (isDuplicateConstraintError(e)) {
                    log.debug("Registro duplicado ignorado: contrato={}, placa={}",
                            maskSensitiveData(dto.contrato()), maskSensitiveData(dto.placa()));
                    duplicateSkipped++;
                } else {
                    log.error("Erro inesperado ao processar veículo: {}", e.getMessage());
                    throw e;
                }
            }
        }

        refreshInMemoryCache();

        log.info("=== RESULTADO DA SINCRONIZAÇÃO ===");
        log.info("{} atualizados (com mudanças)", updated);
        log.info("{} sem mudanças (só sync date)", noChangesFound);
        log.info("{} novos inseridos", inserted);
        log.info("{} duplicados ignorados", duplicateSkipped);
        
        // Enriquecer dados após salvar os básicos
        if (enrichmentService != null && (inserted > 0 || updated > 0)) {
            List<Long> vehicleIdsToEnrich = new ArrayList<>();
            
            // Coletar IDs dos veículos inseridos/atualizados para enriquecimento
            for (VehicleDTO dto : vehicles) {
                Long vehicleId = findVehicleIdByData(dto);
                if (vehicleId != null) {
                    vehicleIdsToEnrich.add(vehicleId);
                }
            }
            
            if (!vehicleIdsToEnrich.isEmpty()) {
                log.info("Iniciando enriquecimento assíncrono de {} veículos", vehicleIdsToEnrich.size());
                enrichmentService.enrichCacheDataAsync(vehicleIdsToEnrich);
            }
        }
    }
    
    private Long findVehicleIdByData(VehicleDTO dto) {
        try {
            if (dto.contrato() != null && !"N/A".equals(dto.contrato())) {
                return contratoToIdCache.getIfPresent(dto.contrato());
            } else if (dto.placa() != null && !"N/A".equals(dto.placa())) {
                return placaToIdCache.getIfPresent(dto.placa());
            }
        } catch (Exception e) {
            log.debug("Erro ao buscar ID do veículo: {}", e.getMessage());
        }
        return null;
    }

    private Optional<VehicleCache> findExistingVehicleOptimized(VehicleDTO dto) {
        String contratoDecrypted = cryptoService.decryptContrato(dto.contrato());
        String placaDecrypted = cryptoService.decryptPlaca(dto.placa());
        String contratoHash = generateHash(contratoDecrypted);
        String placaHash = generateHash(placaDecrypted);
        String contratoPlacaHash = null;

        if (contratoDecrypted != null && placaDecrypted != null) {
            contratoPlacaHash = generateHash(contratoDecrypted + "|" + placaDecrypted);
        }

        if (contratoPlacaHash != null) {
            Optional<VehicleCache> byContratoPlaca = vehicleCacheRepository.findByContratoPlacaHash(contratoPlacaHash);
            if (byContratoPlaca.isPresent()) {
                log.debug("Veículo encontrado por hash contrato+placa");
                return byContratoPlaca;
            }
        }

        if (contratoHash != null) {
            Optional<VehicleCache> byContrato = vehicleCacheRepository.findByContratoHash(contratoHash);
            if (byContrato.isPresent()) {
                log.debug("Veículo encontrado por hash do contrato");
                return byContrato;
            }
        }

        if (placaHash != null) {
            Optional<VehicleCache> byPlaca = vehicleCacheRepository.findByPlacaHash(placaHash);
            if (byPlaca.isPresent()) {
                log.debug("Veículo encontrado por hash da placa");
                return byPlaca;
            }
        }

        if (dto.protocolo() != null && !"N/A".equals(dto.protocolo())) {
            Optional<VehicleCache> byProtocolo = vehicleCacheRepository.findByProtocolo(dto.protocolo());
            if (byProtocolo.isPresent()) {
                log.debug("Veículo encontrado por protocolo");
                return byProtocolo;
            }
        }

        log.debug("Nenhum veículo existente encontrado - será inserido como novo");
        return Optional.empty();
    }

    private synchronized void updateInMemoryCache(VehicleDTO dto, Long vehicleId) {
        try {
            String contratoDecrypted = cryptoService.decryptContrato(dto.contrato());
            if (contratoDecrypted != null && !"N/A".equals(contratoDecrypted)) {
                contratoToIdCache.put(contratoDecrypted.trim(), vehicleId);
            }

            String placaDecrypted = cryptoService.decryptPlaca(dto.placa());
            if (placaDecrypted != null && !"N/A".equals(placaDecrypted)) {
                placaToIdCache.put(placaDecrypted.trim().toUpperCase(), vehicleId);
            }

            if (dto.protocolo() != null && !"N/A".equals(dto.protocolo())) {
                protocoloToIdCache.put(dto.protocolo().trim(), vehicleId);
            }
        } catch (Exception e) {
            log.warn("Erro ao atualizar cache em memória para veículo ID {}: {}", vehicleId, e.getMessage());
        }
    }

    private boolean hasDataChanges(VehicleCache existing, VehicleDTO dto) {
        try {
            String existingContrato = cryptoService.decryptContrato(existing.getContrato());
            String existingPlaca = cryptoService.decryptPlaca(existing.getPlaca());

            String dtoContratoDecrypted = cryptoService.decryptContrato(dto.contrato());
            String dtoPlacaDecrypted = cryptoService.decryptPlaca(dto.placa());

            boolean contratoChanged = !Objects.equals(
                    normalizeString(existingContrato),
                    normalizeString(dtoContratoDecrypted)
            );

            boolean placaChanged = !Objects.equals(
                    normalizeString(existingPlaca),
                    normalizeString(dtoPlacaDecrypted)
            );

            boolean credorChanged = !Objects.equals(
                    normalizeString(existing.getCredor()),
                    normalizeString(dto.credor())
            );

            boolean dataPedidoChanged = !Objects.equals(existing.getDataPedido(), dto.dataPedido());
            boolean modeloChanged = !Objects.equals(normalizeString(existing.getModelo()), normalizeString(dto.modelo()));
            boolean ufChanged = !Objects.equals(normalizeString(existing.getUf()), normalizeString(dto.uf()));
            boolean cidadeChanged = !Objects.equals(normalizeString(existing.getCidade()), normalizeString(dto.cidade()));
            boolean cpfDevedorChanged = !Objects.equals(normalizeString(existing.getCpfDevedor()), normalizeString(dto.cpfDevedor()));
            boolean protocoloChanged = !Objects.equals(normalizeString(existing.getProtocolo()), normalizeString(dto.protocolo()));
            boolean etapaAtualChanged = !Objects.equals(normalizeString(existing.getEtapaAtual()), normalizeString(dto.etapaAtual()));
            boolean statusApreensaoChanged = !Objects.equals(normalizeString(existing.getStatusApreensao()), normalizeString(dto.statusApreensao()));

            boolean ultimaMovimentacaoChanged = !Objects.equals(existing.getUltimaMovimentacao(), dto.ultimaMovimentacao());

            boolean hasChanges = contratoChanged || placaChanged || credorChanged || dataPedidoChanged ||
                    modeloChanged || ufChanged || cidadeChanged || cpfDevedorChanged ||
                    protocoloChanged || etapaAtualChanged || statusApreensaoChanged || ultimaMovimentacaoChanged;

            if (hasChanges) {
                log.debug("Mudanças detectadas: contrato={}, placa={}, credor={}, dataPedido={}, " +
                                "modelo={}, uf={}, cidade={}, cpf={}, etapa={}, status={}, ultimaMov={}",
                        contratoChanged, placaChanged, credorChanged, dataPedidoChanged,
                        modeloChanged, ufChanged, cidadeChanged, cpfDevedorChanged,
                        etapaAtualChanged, statusApreensaoChanged, ultimaMovimentacaoChanged);
            }

            return hasChanges;

        } catch (Exception e) {
            log.warn("Erro ao comparar dados do veículo: {} - assumindo que há mudanças", e.getMessage());
            return true;
        }
    }

    private String normalizeString(String str) {
        if (str == null || "N/A".equals(str)) {
            return null;
        }
        return str.trim();
    }

    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "***";
        }
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }

    private boolean isDuplicateConstraintError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;

        boolean isConstraintError = message.contains("constraint") ||
                message.contains("duplicate") ||
                message.contains("unique") ||
                message.contains("violates unique constraint");

        boolean isHashConstraint = message.contains("unique_contrato_hash") ||
                message.contains("unique_placa_hash") ||
                message.contains("unique_contrato_placa_hash");

        if (isConstraintError && isHashConstraint) {
            log.debug("Registro duplicado detectado pela constraint de hash: {}", message);
        }

        return isConstraintError;
    }

    private VehicleCache updateExistingVehicle(VehicleCache existing, VehicleDTO dto, LocalDateTime syncDate) {
        existing.setCredor(dto.credor());
        existing.setDataPedido(dto.dataPedido());
        existing.setContrato(dto.contrato());
        existing.setPlaca(dto.placa());
        existing.setModelo(dto.modelo());
        existing.setUf(dto.uf());
        existing.setCidade(dto.cidade());
        existing.setCpfDevedor(dto.cpfDevedor());
        existing.setProtocolo(dto.protocolo());
        existing.setEtapaAtual(dto.etapaAtual());
        existing.setStatusApreensao(dto.statusApreensao());
        existing.setUltimaMovimentacao(dto.ultimaMovimentacao());
        existing.setApiSyncDate(syncDate);

        return existing;
    }

    public void cleanDuplicates() {
        log.info("Iniciando limpeza de duplicatas usando cache em memória");

        try {
            Map<String, List<Long>> contratoGroups = new HashMap<>();
            Map<String, List<Long>> placaGroups = new HashMap<>();

            contratoToIdCache.asMap().forEach((key, value) ->
                    contratoGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(value)
            );

            placaToIdCache.asMap().forEach((key, value) ->
                    placaGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(value)
            );

            int deletedCount = 0;

            for (List<Long> duplicateIds : contratoGroups.values()) {
                if (duplicateIds.size() > 1) {
                    deletedCount += removeDuplicates(duplicateIds, "contrato");
                }
            }

            for (List<Long> duplicateIds : placaGroups.values()) {
                if (duplicateIds.size() > 1) {
                    List<Long> stillExisting = duplicateIds.stream()
                            .filter(vehicleCacheRepository::existsById)
                            .collect(Collectors.toList());

                    if (stillExisting.size() > 1) {
                        deletedCount += removeDuplicates(stillExisting, "placa");
                    }
                }
            }

            if (deletedCount > 0) {
                refreshInMemoryCache();
            }

            log.info("Limpeza de duplicatas concluída: {} registros removidos", deletedCount);

        } catch (Exception e) {
            log.error("Erro durante limpeza de duplicatas", e);
        }
    }

    private int removeDuplicates(List<Long> duplicateIds, String campo) {
        if (duplicateIds.size() <= 1) {
            return 0;
        }

        List<VehicleCache> duplicates = vehicleCacheRepository.findAllById(duplicateIds);

        duplicates.sort((a, b) -> {
            if (a.getApiSyncDate() != null && b.getApiSyncDate() != null) {
                return b.getApiSyncDate().compareTo(a.getApiSyncDate());
            }
            return b.getId().compareTo(a.getId());
        });

        List<VehicleCache> toDelete = duplicates.subList(1, duplicates.size());
        vehicleCacheRepository.deleteAll(toDelete);

        log.debug("Removidas {} duplicatas por {}", toDelete.size(), campo);
        return toDelete.size();
    }

    @Transactional
    public void cleanOldCache() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cacheRetentionDays);
        vehicleCacheRepository.deleteOldCacheEntries(cutoffDate);
        log.info("Cache limpo - removidas entradas antigas anteriores a {}", cutoffDate);
    }

    public boolean isCacheValid() {
        Optional<LocalDateTime> lastSyncOpt = vehicleCacheRepository.findLastSyncDate();

        if (lastSyncOpt.isEmpty()) {
            log.warn("Cache vazio - nenhuma sincronização encontrada");
            return false;
        }

        LocalDateTime lastSync = lastSyncOpt.get();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cacheExpiryMinutes);
        boolean isValid = lastSync.isAfter(cutoff);

        if (isValid) {
            log.info("Cache válido - última sincronização: {} (há {} minutos)",
                    lastSync, Duration.between(lastSync, LocalDateTime.now()).toMinutes());
        } else {
            log.warn("Cache desatualizado - última sincronização: {} (há {} minutos)",
                    lastSync, Duration.between(lastSync, LocalDateTime.now()).toMinutes());
        }

        return isValid;
    }

    public Page<VehicleDTO> getFromCache(LocalDate dataInicio, LocalDate dataFim,
                                         String credor, String contrato,
                                         String protocolo, String cpf,
                                         String uf, String cidade,
                                         String modelo, String placa,
                                         String etapaAtual, String statusApreensao,
                                         Pageable pageable) {

        log.info("Buscando dados do PostgreSQL (Cache)");
        log.debug("Filtros originais: dataInicio={}, dataFim={}, credor={}, contrato={}, protocolo={}, cpf={}, " +
                        "uf={}, cidade={}, modelo={}, placa={}, etapaAtual={}, statusApreensao={}",
                dataInicio, dataFim, credor, contrato, protocolo, cpf,
                uf, cidade, modelo, placa, etapaAtual, statusApreensao);

        String contratoHash = null;
        String placaHash = null;

        if (contrato != null && !contrato.trim().isEmpty()) {
            contratoHash = generateHash(contrato.trim());
        }

        if (placa != null && !placa.trim().isEmpty()) {
            placaHash = generateHash(placa.trim().toUpperCase());
        }

        Page<VehicleCache> cachedVehicles = vehicleCacheRepository.findWithFiltersFixed(
                dataInicio, dataFim, credor, contratoHash, protocolo, cpf,
                uf, cidade, modelo, placaHash, etapaAtual, statusApreensao, pageable
        );

        log.info("Dados recuperados do PostgreSQL: {} registros de {} total",
                cachedVehicles.getContent().size(), cachedVehicles.getTotalElements());

        return cachedVehicles.map(this::decryptAndMapToDTO);
    }

    private VehicleDTO decryptAndMapToDTO(VehicleCache entity) {
        try {
            String placaDescriptografada = cryptoService.decryptPlaca(entity.getPlaca());
            String contratoDescriptografado = cryptoService.decryptContrato(entity.getContrato());

            log.debug("Descriptografando dados - Placa: {} chars, Contrato: {} chars",
                    placaDescriptografada != null ? placaDescriptografada.length() : 0,
                    contratoDescriptografado != null ? contratoDescriptografado.length() : 0);

            return new VehicleDTO(
                    entity.getId(),
                    entity.getCredor(),
                    entity.getDataPedido(),
                    contratoDescriptografado,
                    placaDescriptografada,
                    entity.getModelo(),
                    entity.getUf(),
                    entity.getCidade(),
                    entity.getCpfDevedor(),
                    entity.getProtocolo(),
                    entity.getEtapaAtual(),
                    entity.getStatusApreensao(),
                    entity.getUltimaMovimentacao()
            );
        } catch (Exception e) {
            log.error("Erro ao descriptografar dados do veículo ID {}: {}", entity.getId(), e.getMessage());
            return vehicleCacheMapper.toDTO(entity);
        }
    }

    @Transactional
    public void invalidateCache() {
        log.info("Invalidando todo o cache de veículos");
        long recordsBeforeInvalidation = vehicleCacheRepository.count();


        vehicleCacheRepository.deleteAll();
        contratoToIdCache.invalidateAll();
        placaToIdCache.invalidateAll();
        protocoloToIdCache.invalidateAll();

        log.info("Cache invalidado com sucesso - {} registros de veículos removidos",
                recordsBeforeInvalidation);
    }

    public CacheStatus getCacheStatus() {
        Optional<LocalDateTime> lastSyncOpt = vehicleCacheRepository.findLastSyncDate();
        long totalRecords = vehicleCacheRepository.count();

        if (lastSyncOpt.isEmpty()) {
            return CacheStatus.builder()
                    .valid(false)
                    .totalRecords(totalRecords)
                    .message("Cache vazio")
                    .build();
        }

        LocalDateTime lastSync = lastSyncOpt.get();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cacheExpiryMinutes);
        boolean isValid = lastSync.isAfter(cutoff);
        long minutesSinceSync = Duration.between(lastSync, LocalDateTime.now()).toMinutes();

        return CacheStatus.builder()
                .valid(isValid)
                .lastSyncDate(lastSync)
                .totalRecords(totalRecords)
                .minutesSinceLastSync(minutesSinceSync)
                .message(isValid ? "Cache válido (dados sensíveis protegidos)" :
                        String.format("Cache desatualizado (última sync há %d minutos)", minutesSinceSync))
                .build();
    }

    private String generateHash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.trim().toUpperCase().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Erro ao gerar hash SHA-256", e);
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public long countRecordsWithoutHashes() {
        return vehicleCacheRepository.count() - vehicleCacheRepository.countByContratoHashIsNotNullAndPlacaHashIsNotNull();
    }

    @Transactional
    public void populateHashesForExistingRecords() {
        log.info("Iniciando população de hashes para registros existentes");

        List<VehicleCache> recordsWithoutHashes = vehicleCacheRepository.findByContratoHashIsNullOrPlacaHashIsNull();
        int updated = 0;
        int errors = 0;

        for (VehicleCache vehicle : recordsWithoutHashes) {
            try {
                String contratoDecrypted = cryptoService.decryptContrato(vehicle.getContrato());
                String placaDecrypted = cryptoService.decryptPlaca(vehicle.getPlaca());

                vehicle.setContratoHash(generateHash(contratoDecrypted));
                vehicle.setPlacaHash(generateHash(placaDecrypted));

                if (contratoDecrypted != null && placaDecrypted != null) {
                    vehicle.setContratoPlacaHash(generateHash(contratoDecrypted + "|" + placaDecrypted));
                }

                vehicleCacheRepository.save(vehicle);
                updated++;

                if (updated % 100 == 0) {
                    log.info("Progresso: {} registros atualizados", updated);
                }

            } catch (Exception e) {
                log.error("Erro ao popular hash para veículo ID {}: {}", vehicle.getId(), e.getMessage());
                errors++;
            }
        }

        log.info("População de hashes concluída - Atualizados: {}, Erros: {}", updated, errors);
    }

    @Transactional
    public void removeDuplicateVehicles() {
        log.info("Iniciando remoção de veículos duplicados");

        Map<String, List<VehicleCache>> vehiclesByHash = new HashMap<>();
        List<VehicleCache> allVehicles = vehicleCacheRepository.findAll();

        for (VehicleCache vehicle : allVehicles) {
            try {
                String contratoDecrypted = cryptoService.decryptContrato(vehicle.getContrato());
                String placaDecrypted = cryptoService.decryptPlaca(vehicle.getPlaca());

                String uniqueKey = generateHash(contratoDecrypted + "|" + placaDecrypted);
                vehiclesByHash.computeIfAbsent(uniqueKey, k -> new ArrayList<>()).add(vehicle);

            } catch (Exception e) {
                log.error("Erro ao processar veículo ID {} para detecção de duplicatas: {}",
                        vehicle.getId(), e.getMessage());
            }
        }

        int duplicatesRemoved = 0;
        List<Long> idsToRemove = new ArrayList<>();

        for (Map.Entry<String, List<VehicleCache>> entry : vehiclesByHash.entrySet()) {
            List<VehicleCache> duplicates = entry.getValue();

            if (duplicates.size() > 1) {
                duplicates.sort((a, b) -> b.getId().compareTo(a.getId()));

                for (int i = 1; i < duplicates.size(); i++) {
                    idsToRemove.add(duplicates.get(i).getId());
                    duplicatesRemoved++;
                }
            }
        }

        if (!idsToRemove.isEmpty()) {
            vehicleCacheRepository.deleteAllById(idsToRemove);

            log.info("Remoção de duplicatas concluída - {} veículos duplicados removidos", duplicatesRemoved);
        } else {
            log.info("Nenhuma duplicata encontrada");
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class CacheStatus {
        private boolean valid;
        private LocalDateTime lastSyncDate;
        private long totalRecords;
        private long minutesSinceLastSync;
        private String message;
    }
}