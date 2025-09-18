package com.montreal.msiav_bh.config;

import com.montreal.msiav_bh.service.VehicleCacheEnrichmentService;
import com.montreal.msiav_bh.service.VehicleCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class VehicleCacheConfig {
    
    private final VehicleCacheService vehicleCacheService;
    private final VehicleCacheEnrichmentService enrichmentService;
    
    @PostConstruct
    public void configureServices() {
        // Configurar a referência após a inicialização para evitar dependência circular
        vehicleCacheService.setEnrichmentService(enrichmentService);
        log.info("Serviço de enriquecimento configurado no VehicleCacheService");
    }
}