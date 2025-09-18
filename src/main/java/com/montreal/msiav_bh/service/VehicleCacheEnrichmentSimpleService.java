package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCacheEnrichmentSimpleService {

    private final VehicleCacheRepository vehicleCacheRepository;
    private final ApiQueryService apiQueryService;
    private final VehicleCacheCryptoService cryptoService;
    
    /**
     * Enriquece um número limitado de veículos de forma síncrona e simples
     * Ideal para evitar problemas de conexão com o banco
     */
    @Transactional
    public int enrichLimitedVehicles(int limit) {
        log.info("=== ENRIQUECIMENTO SIMPLES - Processando até {} veículos ===", limit);
        
        // Buscar veículos incompletos com limite
        List<VehicleCache> incompleteVehicles = vehicleCacheRepository
                .findVehiclesWithIncompleteData()
                .stream()
                .limit(limit)
                .toList();
        
        if (incompleteVehicles.isEmpty()) {
            log.info("Nenhum veículo incompleto encontrado");
            return 0;
        }
        
        log.info("Encontrados {} veículos para processar", incompleteVehicles.size());
        
        int successCount = 0;
        int errorCount = 0;
        
        for (VehicleCache vehicle : incompleteVehicles) {
            try {
                boolean enriched = enrichSingleVehicleSimple(vehicle);
                if (enriched) {
                    successCount++;
                    log.info("✅ Veículo {} enriquecido ({}/{})", 
                        vehicle.getId(), successCount, incompleteVehicles.size());
                }
                
                // Pequena pausa para não sobrecarregar
                Thread.sleep(500);
                
            } catch (Exception e) {
                errorCount++;
                log.error("❌ Erro ao enriquecer veículo {}: {}", vehicle.getId(), e.getMessage());
            }
        }
        
        log.info("=== ENRIQUECIMENTO CONCLUÍDO ===");
        log.info("✅ Sucesso: {} veículos", successCount);
        log.info("❌ Erros: {} veículos", errorCount);
        
        return successCount;
    }
    
    private boolean enrichSingleVehicleSimple(VehicleCache vehicle) {
        try {
            // Verificar se tem placa válida
            String placa = cryptoService.decryptPlaca(vehicle.getPlaca());
            if (placa == null || "N/A".equals(placa) || placa.trim().isEmpty()) {
                log.debug("Veículo {} sem placa válida", vehicle.getId());
                return false;
            }
            
            // Buscar dados da API
            log.debug("Buscando dados para placa: {}", placa);
            QueryDetailResponseDTO response = apiQueryService.doSearchContract(placa);
            
            if (response == null || !response.success() || response.data() == null) {
                log.debug("Sem dados detalhados para placa: {}", placa);
                return false;
            }
            
            boolean updated = false;
            
            // Atualizar protocolo
            if (response.data().contrato() != null && response.data().contrato().protocolo() != null) {
                String protocolo = response.data().contrato().protocolo();
                if (!"N/A".equals(protocolo) && !protocolo.trim().isEmpty()) {
                    vehicle.setProtocolo(cryptoService.encryptProtocolo(protocolo));
                    log.debug("Protocolo atualizado: {}", protocolo);
                    updated = true;
                }
            }
            
            // Atualizar cidade
            if (response.data().credor() != null && response.data().credor().endereco() != null) {
                String cidade = extractCity(response.data().credor().endereco());
                if (!"N/A".equals(cidade)) {
                    vehicle.setCidade(cryptoService.encryptCidade(cidade));
                    log.debug("Cidade atualizada: {}", cidade);
                    updated = true;
                }
            }
            
            // Atualizar CPF
            if (response.data().devedores() != null && !response.data().devedores().isEmpty()) {
                String cpf = response.data().devedores().get(0).cpfCnpj();
                if (cpf != null && !"N/A".equals(cpf) && !cpf.trim().isEmpty()) {
                    vehicle.setCpfDevedor(cryptoService.encryptCpfDevedor(cpf));
                    log.debug("CPF atualizado: {}", cpf);
                    updated = true;
                }
            }
            
            if (updated) {
                vehicleCacheRepository.save(vehicle);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Erro ao processar veículo {}: {}", vehicle.getId(), e.getMessage());
            return false;
        }
    }
    
    private String extractCity(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "N/A";
        }
        
        // Remover CEP
        String clean = address.replaceAll("\\d{5}-?\\d{3}", "").replaceAll("Cep:.*", "").trim();
        
        // Tentar extrair cidade (formato comum: ... - Cidade - UF)
        String[] parts = clean.split(" - ");
        if (parts.length >= 2) {
            // Pegar penúltimo elemento (antes do estado)
            String city = parts[parts.length - 2].trim();
            if (!city.isEmpty() && !city.matches("[A-Z]{2}")) {
                return city;
            }
        }
        
        return "N/A";
    }
}