package com.montreal.msiav_bh.job;

import com.montreal.msiav_bh.context.CacheUpdateContext;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO;
import com.montreal.msiav_bh.mapper.VehicleInquiryMapper;
import com.montreal.msiav_bh.service.ApiQueryService;
import com.montreal.msiav_bh.service.VehicleCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleCacheUpdateJob {

    private final ApiQueryService apiQueryService;
    private final VehicleInquiryMapper vehicleInquiryMapper;
    private final VehicleCacheService vehicleCacheService;

    private final ReentrantLock jobLock = new ReentrantLock();

    private volatile boolean isJobRunning = false;
    private volatile LocalDateTime lastJobStart = null;
    private volatile LocalDateTime lastJobEnd = null;

    @Value("${vehicle.cache.update.enabled:true}")
    private boolean cacheUpdateEnabled;

    @Value("${vehicle.cache.update.days-to-fetch:30}")
    private int daysToFetch;

    @Value("${vehicle.cache.update.fallback-days:60}")
    private int fallbackDaysToFetch;

    @Value("${vehicle.cache.update.max-historical-days:180}")
    private int maxHistoricalDays;

    @Value("${vehicle.cache.update.max-execution-minutes:30}")
    private int maxExecutionMinutes;

    @Scheduled(fixedDelayString = "${vehicle.cache.update.interval:600000}")
    public void updateVehicleCache() {
        if (!canExecuteJob()) {
            return;
        }

        boolean lockAcquired = false;
        try {
            lockAcquired = jobLock.tryLock(30, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("Não foi possível adquirir lock do job em 30 segundos - pulando execução");
                return;
            }

            if (isJobRunning) {
                log.info("Job já está executando - pulando");
                return;
            }

            executeJobSafely();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Job interrompido durante aquisição do lock");
        } finally {
            if (lockAcquired) {
                jobLock.unlock();
            }
        }
    }

    private boolean canExecuteJob() {
        if (!cacheUpdateEnabled) {
            log.debug("Atualização automática do cache está desabilitada");
            return false;
        }

        if (isJobRunning) {
            log.debug("Job já está executando");
            return false;
        }

        if (lastJobStart != null && lastJobEnd == null) {
            long minutesSinceStart = ChronoUnit.MINUTES.between(lastJobStart, LocalDateTime.now());
            if (minutesSinceStart > maxExecutionMinutes) {
                log.error("Job anterior iniciado há {} minutos sem terminar - forçando reset", minutesSinceStart);
                resetJobState();
                return true;
            } else {
                log.debug("Job anterior ainda executando há {} minutos", minutesSinceStart);
                return false;
            }
        }

        if (lastJobEnd != null) {
            long minutesSinceLastEnd = ChronoUnit.MINUTES.between(lastJobEnd, LocalDateTime.now());
            if (minutesSinceLastEnd < 5) {
                log.debug("Job executado há apenas {} minutos - aguardando", minutesSinceLastEnd);
                return false;
            }
        }

        return true;
    }

    private void resetJobState() {
        isJobRunning = false;
        lastJobStart = null;
        lastJobEnd = null;
        log.info("Estado do job resetado");
    }

    private void executeJobSafely() {
        isJobRunning = true;
        lastJobStart = LocalDateTime.now();
        lastJobEnd = null;

        try {
            log.info("==== INICIANDO JOB DE ATUALIZAÇÃO DO CACHE ====");
            log.info("Horário: {}", lastJobStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            VehicleCacheService.CacheStatus statusAntes = vehicleCacheService.getCacheStatus();
            log.info("Status do cache antes da atualização: {}", statusAntes.getMessage());

            List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                    tryFetchWithDateRange(daysToFetch, "período principal");

            if ((notifications == null || notifications.isEmpty()) && fallbackDaysToFetch > daysToFetch) {
                log.info("Tentando período de fallback de {} dias", fallbackDaysToFetch);
                notifications = tryFetchWithDateRange(fallbackDaysToFetch, "período de fallback");
            }

            if (notifications == null || notifications.isEmpty()) {
                notifications = tryHistoricalPeriods();
            }

            if (!notifications.isEmpty()) {
                log.info("API retornou {} notificações", notifications.size());

                List<VehicleDTO> vehicles = vehicleInquiryMapper.mapToVeiculoDTO(notifications);
                log.info("Convertidos para {} veículos únicos criptografados", vehicles.size());

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(daysToFetch);
                CacheUpdateContext context = CacheUpdateContext.scheduledRefresh(startDate, endDate);

                log.info("Salvando dados criptografados no PostgreSQL...");
                vehicleCacheService.updateCacheThreadSafe(vehicles, context);

                VehicleCacheService.CacheStatus statusDepois = vehicleCacheService.getCacheStatus();

                long duration = java.time.Duration.between(lastJobStart, LocalDateTime.now()).toSeconds();
                log.info("==== JOB CONCLUÍDO COM SUCESSO ====");
                log.info("Tempo de execução: {} segundos", duration);
                log.info("Veículos processados: {}", vehicles.size());
                log.info("Status após atualização: {}", statusDepois.getMessage());
                log.info("Registros no cache: {} -> {}", statusAntes.getTotalRecords(), statusDepois.getTotalRecords());
                log.info("Próxima execução em 10 minutos");
            } else {
                log.warn("==== NENHUM DADO ENCONTRADO EM TODOS OS PERÍODOS TENTADOS ====");
                log.warn("Verifique se há dados disponíveis na API externa ou se as datas estão corretas");
                log.warn("Cache atual será mantido até próxima sincronização bem-sucedida");
            }

        } catch (Exception e) {
            log.error("==== FALHA NO JOB DE ATUALIZAÇÃO ====", e);

            if (e.getMessage() != null && e.getMessage().contains("criptografia")) {
                log.error("ERRO CRÍTICO DE CRIPTOGRAFIA - Job abortado para evitar dados inconsistentes!");
                log.error("Verifique a conectividade com PostgreSQL e as funções de criptografia");
                return;
            }

            log.error("Os dados continuarão sendo servidos do PostgreSQL (podem estar desatualizados)");
        } finally {
            isJobRunning = false;
            lastJobEnd = LocalDateTime.now();

            long executionTime = java.time.Duration.between(lastJobStart, lastJobEnd).toSeconds();
            log.info("Job finalizado após {} segundos", executionTime);
        }
    }

    private List<ConsultaNotificationResponseDTO.NotificationData> tryFetchWithDateRange(int days, String periodName) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);

            log.info("Tentando {} - Período de busca: {} a {}", periodName, startDate, endDate);
            log.info("Consultando API externa...");

            return apiQueryService.searchByPeriod(startDate, endDate);
        } catch (Exception e) {
            log.warn("Erro ao buscar dados para {}: {}", periodName, e.getMessage());
            return List.of();
        }
    }

    private List<ConsultaNotificationResponseDTO.NotificationData> tryHistoricalPeriods() {
        log.info("Tentando períodos históricos...");

        LocalDate currentDate = LocalDate.now();

        for (int monthsBack = 1; monthsBack <= 6; monthsBack++) {
            try {
                LocalDate endDate = currentDate.minusMonths(monthsBack);
                LocalDate startDate = endDate.minusDays(30);

                if (ChronoUnit.DAYS.between(startDate, currentDate) > maxHistoricalDays) {
                    break;
                }

                log.info("Tentando período histórico: {} a {} ({} meses atrás)",
                        startDate, endDate, monthsBack);

                List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                        apiQueryService.searchByPeriod(startDate, endDate);

                if (notifications != null && !notifications.isEmpty()) {
                    log.info("Encontrados dados no período histórico de {} meses atrás", monthsBack);
                    return notifications;
                }
            } catch (Exception e) {
                log.debug("Erro ao buscar período histórico de {} meses: {}", monthsBack, e.getMessage());
            }
        }

        log.warn("Nenhum dado encontrado em períodos históricos");
        return List.of();
    }

    public String getJobStatus() {
        if (isJobRunning && lastJobStart != null) {
            long minutesRunning = ChronoUnit.MINUTES.between(lastJobStart, LocalDateTime.now());
            return String.format("Job executando há %d minutos", minutesRunning);
        } else if (lastJobEnd != null) {
            long minutesSinceEnd = ChronoUnit.MINUTES.between(lastJobEnd, LocalDateTime.now());
            return String.format("Job finalizado há %d minutos", minutesSinceEnd);
        } else {
            return "Job nunca executado";
        }
    }

    @Scheduled(cron = "${vehicle.cache.cleanup.cron:0 0 2 * * ?}")
    public void cleanupOldCache() {
        if (!jobLock.tryLock()) {
            log.info("Job de atualização em andamento - pulando limpeza do cache");
            return;
        }

        try {
            log.info("==== INICIANDO LIMPEZA DO CACHE ====");
            log.info("Horário: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            try {
                vehicleCacheService.cleanOldCache();
                log.info("Limpeza do cache concluída com sucesso");
            } catch (Exception e) {
                log.error("Falha na limpeza do cache", e);
            }
        } finally {
            jobLock.unlock();
        }
    }

    @Scheduled(cron = "${vehicle.cache.duplicate.cleanup.cron:0 30 1 * * ?}")
    public void cleanupDuplicates() {
        if (!jobLock.tryLock()) {
            log.info("Job de atualização em andamento - pulando limpeza de duplicatas");
            return;
        }

        try {
            log.info("==== INICIANDO LIMPEZA DE DUPLICATAS ====");
            log.info("Horário: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            try {
                vehicleCacheService.removeDuplicateVehicles();
                log.info("Limpeza de duplicatas concluída com sucesso");
            } catch (Exception e) {
                log.error("Falha na limpeza de duplicatas", e);
            }
        } finally {
            jobLock.unlock();
        }
    }

    @PostConstruct
    public void populateHashesIfNeeded() {
        log.info("Verificando se é necessário popular hashes em registros existentes...");

        try {
            long totalRecords = vehicleCacheService.countRecordsWithoutHashes();
            if (totalRecords > 0) {
                log.info("Encontrados {} registros sem hashes. Iniciando população...", totalRecords);
                vehicleCacheService.populateHashesForExistingRecords();
                log.info("População de hashes concluída");
            } else {
                log.info("Todos os registros já possuem hashes");
            }
        } catch (Exception e) {
            log.error("Erro ao popular hashes: {}", e.getMessage(), e);
        }
    }
}