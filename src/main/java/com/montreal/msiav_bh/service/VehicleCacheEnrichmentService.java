package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCacheEnrichmentService {

    private final VehicleCacheRepository vehicleCacheRepository;
    private final ApiQueryService apiQueryService;
    private final VehicleCacheCryptoService cryptoService;
    
    private static final int MAX_CONCURRENT_REQUESTS = 5; // Limitar requisições simultâneas
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    
    /**
     * Enriquece os dados do cache buscando informações detalhadas via API
     * Este método é executado de forma assíncrona após a atualização básica do cache
     */
    @Async
    public CompletableFuture<Void> enrichCacheDataAsync(List<Long> vehicleIds) {
        log.info("=== INICIANDO ENRIQUECIMENTO ASSÍNCRONO ===");
        log.info("Total de veículos para enriquecer: {}", vehicleIds.size());
        
        // Processar em lotes menores para evitar timeout
        int batchSize = 10;
        List<List<Long>> batches = new ArrayList<>();
        
        for (int i = 0; i < vehicleIds.size(); i += batchSize) {
            batches.add(vehicleIds.subList(i, Math.min(i + batchSize, vehicleIds.size())));
        }
        
        log.info("Dividido em {} lotes de até {} veículos", batches.size(), batchSize);
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        
        try {
            for (int i = 0; i < batches.size(); i++) {
                List<Long> batch = batches.get(i);
                log.info("Processando lote {}/{} com {} veículos", i + 1, batches.size(), batch.size());
                
                List<CompletableFuture<Void>> futures = batch.stream()
                        .map(id -> CompletableFuture.runAsync(() -> enrichSingleVehicle(id), executor))
                        .collect(Collectors.toList());
                
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .get(batch.size() * REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("Erro no lote {}: {}", i + 1, e.getMessage());
                }
                
                // Pequena pausa entre lotes para não sobrecarregar
                Thread.sleep(1000);
            }
                    
            log.info("✅ Enriquecimento concluído para {} veículos", vehicleIds.size());
            
        } catch (Exception e) {
            log.error("Erro durante o enriquecimento em lote: {}", e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Enriquece os dados de um único veículo
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void enrichSingleVehicle(Long vehicleId) {
        try {
            log.debug("Processando enriquecimento do veículo ID: {}", vehicleId);
            
            VehicleCache vehicle = vehicleCacheRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                log.warn("Veículo com ID {} não encontrado no cache", vehicleId);
                return;
            }
            
            // Verifica se já tem os dados completos
            if (hasCompleteData(vehicle)) {
                log.debug("Veículo {} já possui dados completos, pulando enriquecimento", vehicleId);
                return;
            }
            
            String placa = cryptoService.decryptPlaca(vehicle.getPlaca());
            log.debug("Placa descriptografada para veículo {}: {}", vehicleId, placa);
            
            if (placa == null || "N/A".equals(placa) || placa.trim().isEmpty()) {
                log.debug("Veículo {} sem placa válida, não é possível enriquecer", vehicleId);
                return;
            }
            
            log.info("Buscando dados detalhados para veículo {} com placa {}", vehicleId, placa);
            
            // Busca dados detalhados via API
            QueryDetailResponseDTO detailedData = null;
            try {
                detailedData = apiQueryService.doSearchContract(placa);
            } catch (Exception apiEx) {
                log.error("Erro ao chamar API para veículo {}: {}", vehicleId, apiEx.getMessage());
                return;
            }
            
            if (detailedData != null && detailedData.success() && detailedData.data() != null) {
                log.info("Dados detalhados obtidos com sucesso para veículo {}", vehicleId);
                updateVehicleWithDetailedData(vehicle, detailedData.data());
                vehicleCacheRepository.save(vehicle);
                vehicleCacheRepository.flush(); // Forçar gravação imediata
                log.info("✅ Veículo {} enriquecido e salvo com sucesso", vehicleId);
            } else {
                log.warn("Não foi possível obter dados detalhados para veículo {} (placa: {})", vehicleId, placa);
            }
            
        } catch (Exception e) {
            log.error("Erro ao enriquecer veículo {}: {}", vehicleId, e.getMessage());
            // Não propagar o erro para não afetar outros veículos
        }
    }
    
    /**
     * Verifica se o veículo já possui dados completos
     */
    private boolean hasCompleteData(VehicleCache vehicle) {
        String protocolo = cryptoService.decryptProtocolo(vehicle.getProtocolo());
        String cidade = cryptoService.decryptCidade(vehicle.getCidade());
        String cpfDevedor = cryptoService.decryptCpfDevedor(vehicle.getCpfDevedor());
        
        return protocolo != null && !"N/A".equals(protocolo) &&
               cidade != null && !"N/A".equals(cidade) &&
               cpfDevedor != null && !"N/A".equals(cpfDevedor);
    }
    
    /**
     * Atualiza o veículo com os dados detalhados da API
     */
    private void updateVehicleWithDetailedData(VehicleCache vehicle, QueryDetailResponseDTO.Data data) {
        log.debug("Atualizando dados do veículo ID: {}", vehicle.getId());
        
        // Atualiza protocolo
        if (data.contrato() != null && data.contrato().protocolo() != null) {
            String protocolo = data.contrato().protocolo();
            log.debug("Protocolo encontrado: {}", protocolo);
            if (!"N/A".equals(protocolo) && !protocolo.trim().isEmpty()) {
                String protocoloCriptografado = cryptoService.encryptProtocolo(protocolo);
                vehicle.setProtocolo(protocoloCriptografado);
                log.info("✅ Protocolo atualizado para veículo {}: {} (criptografado: {} chars)", 
                    vehicle.getId(), protocolo, protocoloCriptografado.length());
            }
        } else {
            log.debug("Protocolo não encontrado nos dados detalhados");
        }
        
        // Atualiza cidade (extraída do endereço do credor)
        if (data.credor() != null && data.credor().endereco() != null) {
            String enderecoCompleto = data.credor().endereco();
            log.debug("Endereço do credor: {}", enderecoCompleto);
            String cidade = extractCityFromAddress(enderecoCompleto);
            log.debug("Cidade extraída: {}", cidade);
            if (!"N/A".equals(cidade)) {
                String cidadeCriptografada = cryptoService.encryptCidade(cidade);
                vehicle.setCidade(cidadeCriptografada);
                log.info("✅ Cidade atualizada para veículo {}: {} (criptografada: {} chars)", 
                    vehicle.getId(), cidade, cidadeCriptografada.length());
            }
        } else {
            log.debug("Endereço do credor não encontrado nos dados detalhados");
        }
        
        // Atualiza CPF do devedor
        if (data.devedores() != null && !data.devedores().isEmpty()) {
            String cpfDevedor = data.devedores().get(0).cpfCnpj();
            log.debug("CPF do devedor encontrado: {}", cpfDevedor);
            if (cpfDevedor != null && !"N/A".equals(cpfDevedor) && !cpfDevedor.trim().isEmpty()) {
                String cpfCriptografado = cryptoService.encryptCpfDevedor(cpfDevedor);
                vehicle.setCpfDevedor(cpfCriptografado);
                log.info("✅ CPF do devedor atualizado para veículo {}: {} (criptografado: {} chars)", 
                    vehicle.getId(), cpfDevedor, cpfCriptografado.length());
            }
        } else {
            log.debug("Devedores não encontrados nos dados detalhados");
        }
    }
    
    /**
     * Extrai a cidade do endereço completo
     */
    private String extractCityFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "N/A";
        }
        
        // Remove CEP
        String cleanAddress = address.replaceAll("\\d{5}-?\\d{3}", "").trim();
        
        // Procura por padrões comuns de endereço brasileiro
        // Formato: Rua X, Número - Bairro - Cidade - UF
        String[] parts = cleanAddress.split(" - ");
        
        if (parts.length >= 3) {
            // Normalmente a cidade é o penúltimo elemento (antes do UF)
            String possibleCity = parts[parts.length - 2].trim();
            
            // Remove "Cep:" se existir
            possibleCity = possibleCity.replaceAll("(?i)cep:?.*", "").trim();
            
            if (!possibleCity.isEmpty() && !possibleCity.matches("[A-Z]{2}")) {
                return possibleCity;
            }
        }
        
        // Tenta outro padrão com vírgula
        parts = cleanAddress.split(",");
        if (parts.length >= 2) {
            for (int i = parts.length - 1; i >= 0; i--) {
                String part = parts[i].trim();
                // Remove estado (duas letras maiúsculas)
                part = part.replaceAll("\\b[A-Z]{2}\\b", "").trim();
                // Remove números
                part = part.replaceAll("\\d+", "").trim();
                
                if (part.length() > 2 && !part.matches(".*\\d.*")) {
                    return part;
                }
            }
        }
        
        return "N/A";
    }
    
    /**
     * Enriquece veículos que não possuem dados completos
     * Método para ser chamado periodicamente ou sob demanda
     */
    @Transactional(readOnly = true)  // Apenas leitura para evitar problemas de transação
    public void enrichIncompleteVehicles() {
        log.info("=== INICIANDO BUSCA DE VEÍCULOS INCOMPLETOS ===");
        
        List<Long> vehicleIds;
        try {
            // Busca veículos sem protocolo, cidade ou CPF
            List<VehicleCache> incompleteVehicles = vehicleCacheRepository.findVehiclesWithIncompleteData();
            
            if (incompleteVehicles.isEmpty()) {
                log.info("✅ Nenhum veículo com dados incompletos encontrado - cache já está completo!");
                return;
            }
            
            log.info("📊 Encontrados {} veículos com dados incompletos", incompleteVehicles.size());
            
            // Log alguns exemplos
            incompleteVehicles.stream().limit(5).forEach(v -> {
                log.debug("Veículo ID {} - Protocolo: {}, Cidade: {}, CPF: {}", 
                    v.getId(), 
                    v.getProtocolo(), 
                    v.getCidade(), 
                    v.getCpfDevedor());
            });
            
            vehicleIds = incompleteVehicles.stream()
                    .map(VehicleCache::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao buscar veículos incompletos: {}", e.getMessage());
            throw e;
        }
        
        // Iniciar enriquecimento fora da transação de leitura
        log.info("🚀 Iniciando enriquecimento assíncrono de {} veículos", vehicleIds.size());
        enrichCacheDataAsync(vehicleIds);
    }
}