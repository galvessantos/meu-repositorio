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
    @Transactional
    public CompletableFuture<Void> enrichCacheDataAsync(List<Long> vehicleIds) {
        log.info("Iniciando enriquecimento assíncrono de {} veículos", vehicleIds.size());
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        
        try {
            List<CompletableFuture<Void>> futures = vehicleIds.stream()
                    .map(id -> CompletableFuture.runAsync(() -> enrichSingleVehicle(id), executor))
                    .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(vehicleIds.size() * REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    
            log.info("Enriquecimento concluído com sucesso para {} veículos", vehicleIds.size());
            
        } catch (Exception e) {
            log.error("Erro durante o enriquecimento em lote: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Enriquece os dados de um único veículo
     */
    private void enrichSingleVehicle(Long vehicleId) {
        try {
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
            if (placa == null || "N/A".equals(placa)) {
                log.debug("Veículo {} sem placa válida, não é possível enriquecer", vehicleId);
                return;
            }
            
            // Busca dados detalhados via API
            QueryDetailResponseDTO detailedData = apiQueryService.doSearchContract(placa);
            
            if (detailedData != null && detailedData.success() && detailedData.data() != null) {
                updateVehicleWithDetailedData(vehicle, detailedData.data());
                vehicleCacheRepository.save(vehicle);
                log.debug("Veículo {} enriquecido com sucesso", vehicleId);
            }
            
        } catch (Exception e) {
            log.error("Erro ao enriquecer veículo {}: {}", vehicleId, e.getMessage());
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
        // Atualiza protocolo
        if (data.contrato() != null && data.contrato().protocolo() != null) {
            String protocolo = data.contrato().protocolo();
            if (!"N/A".equals(protocolo) && !protocolo.trim().isEmpty()) {
                vehicle.setProtocolo(cryptoService.encryptProtocolo(protocolo));
                log.trace("Protocolo atualizado para veículo {}", vehicle.getId());
            }
        }
        
        // Atualiza cidade (extraída do endereço do credor)
        if (data.credor() != null && data.credor().endereco() != null) {
            String cidade = extractCityFromAddress(data.credor().endereco());
            if (!"N/A".equals(cidade)) {
                vehicle.setCidade(cryptoService.encryptCidade(cidade));
                log.trace("Cidade atualizada para veículo {}: {}", vehicle.getId(), cidade);
            }
        }
        
        // Atualiza CPF do devedor
        if (data.devedores() != null && !data.devedores().isEmpty()) {
            String cpfDevedor = data.devedores().get(0).cpfCnpj();
            if (cpfDevedor != null && !"N/A".equals(cpfDevedor) && !cpfDevedor.trim().isEmpty()) {
                vehicle.setCpfDevedor(cryptoService.encryptCpfDevedor(cpfDevedor));
                log.trace("CPF do devedor atualizado para veículo {}", vehicle.getId());
            }
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
    @Transactional
    public void enrichIncompleteVehicles() {
        log.info("Buscando veículos com dados incompletos para enriquecimento");
        
        // Busca veículos sem protocolo, cidade ou CPF
        List<VehicleCache> incompleteVehicles = vehicleCacheRepository.findVehiclesWithIncompleteData();
        
        if (incompleteVehicles.isEmpty()) {
            log.info("Nenhum veículo com dados incompletos encontrado");
            return;
        }
        
        log.info("Encontrados {} veículos com dados incompletos", incompleteVehicles.size());
        
        List<Long> vehicleIds = incompleteVehicles.stream()
                .map(VehicleCache::getId)
                .collect(Collectors.toList());
                
        enrichCacheDataAsync(vehicleIds);
    }
}