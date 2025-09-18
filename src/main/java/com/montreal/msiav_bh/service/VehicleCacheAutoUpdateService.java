package com.montreal.msiav_bh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Serviço de atualização automática inteligente do cache
 * Garante que os dados estejam sempre completos e atualizados
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCacheAutoUpdateService {

    private final VehicleCacheService vehicleCacheService;
    private final VehicleDataFetchService vehicleDataFetchService;
    private final VehicleApiService vehicleApiService;
    
    @Value("${vehicle.cache.auto-update.enabled:true}")
    private boolean autoUpdateEnabled;
    
    @Value("${vehicle.cache.auto-update.on-startup:true}")
    private boolean updateOnStartup;
    
    @Value("${vehicle.cache.auto-update.if-incomplete:true}")
    private boolean updateIfIncomplete;
    
    /**
     * Executa ao iniciar a aplicação
     */
    @PostConstruct
    public void initializeCache() {
        if (!autoUpdateEnabled) {
            log.info("Auto-update desabilitado via configuração");
            return;
        }
        
        log.info("=== VERIFICANDO CACHE NA INICIALIZAÇÃO ===");
        
        try {
            VehicleCacheService.CacheStatus status = vehicleCacheService.getCacheStatus();
            
            // Cenário 1: Cache vazio - sempre atualizar
            if (status.getTotalRecords() == 0) {
                log.warn("⚠️ Cache vazio detectado - iniciando carga completa");
                performCompleteUpdate();
                return;
            }
            
            // Cenário 2: Cache com dados incompletos
            if (updateIfIncomplete) {
                long incompleteCount = vehicleCacheService.getVehiclesWithIncompleteData().size();
                if (incompleteCount > 0) {
                    log.warn("⚠️ Encontrados {} veículos com dados incompletos", incompleteCount);
                    
                    // Se mais de 10% está incompleto, fazer atualização completa
                    double percentIncomplete = (double) incompleteCount / status.getTotalRecords() * 100;
                    if (percentIncomplete > 10) {
                        log.info("{}% dos dados estão incompletos - fazendo atualização completa", 
                            String.format("%.1f", percentIncomplete));
                        performCompleteUpdate();
                        return;
                    }
                }
            }
            
            // Cenário 3: Cache muito antigo (mais de 24 horas)
            if (status.getMinutesSinceLastSync() > 1440) { // 24 horas
                log.warn("⚠️ Cache com mais de 24 horas - atualizando");
                performCompleteUpdate();
                return;
            }
            
            // Cenário 4: Forçar atualização na inicialização (configurável)
            if (updateOnStartup) {
                log.info("Atualização na inicialização habilitada - atualizando cache");
                performCompleteUpdate();
                return;
            }
            
            log.info("✅ Cache válido e completo - {} registros, última atualização há {} minutos",
                status.getTotalRecords(), status.getMinutesSinceLastSync());
                
        } catch (Exception e) {
            log.error("Erro ao verificar cache na inicialização: {}", e.getMessage());
        }
    }
    
    /**
     * Atualização agendada - executa todos os dias às 6h da manhã
     */
    @Scheduled(cron = "${vehicle.cache.auto-update.cron:0 0 6 * * *}")
    public void scheduledCacheUpdate() {
        if (!autoUpdateEnabled) {
            return;
        }
        
        log.info("=== ATUALIZAÇÃO AGENDADA DO CACHE ===");
        performCompleteUpdate();
    }
    
    /**
     * Verifica a cada hora se precisa atualizar
     */
    @Scheduled(fixedDelay = 3600000) // 1 hora
    public void checkCacheHealth() {
        if (!autoUpdateEnabled) {
            return;
        }
        
        try {
            VehicleCacheService.CacheStatus status = vehicleCacheService.getCacheStatus();
            
            // Se o cache está muito desatualizado, atualizar
            if (status.getMinutesSinceLastSync() > 360) { // 6 horas
                log.info("Cache desatualizado ({} horas) - iniciando atualização", 
                    status.getMinutesSinceLastSync() / 60);
                performCompleteUpdate();
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do cache: {}", e.getMessage());
        }
    }
    
    /**
     * Realiza atualização completa com dados enriquecidos
     */
    private void performCompleteUpdate() {
        try {
            log.info("🔄 Iniciando atualização completa do cache com dados enriquecidos");
            
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            // Usar o serviço otimizado que já traz dados completos
            vehicleApiService.forceRefreshFromApi();
            
            // Verificar resultado
            VehicleCacheService.CacheStatus newStatus = vehicleCacheService.getCacheStatus();
            long incompleteAfter = vehicleCacheService.getVehiclesWithIncompleteData().size();
            
            log.info("✅ Atualização completa finalizada:");
            log.info("   - Total de registros: {}", newStatus.getTotalRecords());
            log.info("   - Registros incompletos: {}", incompleteAfter);
            log.info("   - Taxa de completude: {}%", 
                String.format("%.1f", (1 - (double)incompleteAfter/newStatus.getTotalRecords()) * 100));
                
        } catch (Exception e) {
            log.error("❌ Falha na atualização automática do cache: {}", e.getMessage());
        }
    }
    
    /**
     * Método para forçar atualização manual
     */
    public void forceUpdate() {
        log.info("Atualização manual solicitada");
        performCompleteUpdate();
    }
}