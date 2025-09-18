package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO;
import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleDataFetchService {

    private final ApiQueryService apiQueryService;
    private final VehicleCacheCryptoService cryptoService;

    private static final int THREAD_POOL_SIZE = 10;
    private static final int BATCH_SIZE = 50;

    public List<VehicleDTO> fetchCompleteVehicleData(LocalDate startDate, LocalDate endDate) {
        log.info("=== INICIANDO BUSCA OTIMIZADA DE DADOS COMPLETOS ===");
        log.info("Período: {} a {}", startDate, endDate);

        try {
            List<ConsultaNotificationResponseDTO.NotificationData> basicData =
                    apiQueryService.searchByPeriod(startDate, endDate);

            if (basicData.isEmpty()) {
                log.info("Nenhum veículo encontrado no período");
                return new ArrayList<>();
            }

            log.info("Encontrados {} registros básicos", basicData.size());

            Map<String, VehicleCompleteData> vehicleMap = new ConcurrentHashMap<>();

            for (ConsultaNotificationResponseDTO.NotificationData notification : basicData) {
                processBasicNotification(notification, vehicleMap);
            }

            log.info("Mapeados {} veículos únicos", vehicleMap.size());

            fetchDetailedDataInParallel(vehicleMap);

            List<VehicleDTO> completeVehicles = vehicleMap.values().stream()
                    .map(this::convertToVehicleDTO)
                    .collect(Collectors.toList());

            log.info("Busca completa finalizada: {} veículos com dados completos", completeVehicles.size());

            return completeVehicles;

        } catch (Exception e) {
            log.error("Erro na busca otimizada de dados: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao buscar dados completos dos veículos", e);
        }
    }

    private void processBasicNotification(
            ConsultaNotificationResponseDTO.NotificationData notification,
            Map<String, VehicleCompleteData> vehicleMap) {

        String contrato = extractContrato(notification);
        String placa = extractPlaca(notification);

        if (placa == null || "N/A".equals(placa)) {
            log.debug("Notificação sem placa válida, pulando");
            return;
        }

        String key = generateKey(contrato, placa);

        VehicleCompleteData data = vehicleMap.computeIfAbsent(key, k -> new VehicleCompleteData());

        data.contrato = contrato;
        data.placa = placa;
        data.credor = extractCredor(notification);
        data.dataPedido = extractDataPedido(notification);
        data.modelo = extractModelo(notification);
        data.uf = extractUF(notification);
        data.etapaAtual = extractEtapa(notification);
        data.statusApreensao = extractStatus(notification);
        data.ultimaMovimentacao = extractMovimentacao(notification);

        data.protocolo = extractProtocoloBasic(notification);
        data.cidade = "N/A";
        data.cpfDevedor = extractCpfBasic(notification);
    }

    private void fetchDetailedDataInParallel(Map<String, VehicleCompleteData> vehicleMap) {
        log.info("Iniciando busca de dados detalhados em paralelo para {} veículos", vehicleMap.size());

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            List<List<VehicleCompleteData>> batches = createBatches(
                    new ArrayList<>(vehicleMap.values()), BATCH_SIZE
            );

            log.info("Dividido em {} batches de até {} veículos", batches.size(), BATCH_SIZE);

            for (int i = 0; i < batches.size(); i++) {
                List<VehicleCompleteData> batch = batches.get(i);
                log.info("Processando batch {}/{}", i + 1, batches.size());

                List<CompletableFuture<Void>> futures = batch.stream()
                        .map(vehicle -> CompletableFuture.runAsync(
                                () -> fetchVehicleDetails(vehicle), executor
                        ))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

        } finally {
            executor.shutdown();
        }
    }

    private void fetchVehicleDetails(VehicleCompleteData vehicle) {
        if (vehicle.placa == null || "N/A".equals(vehicle.placa)) {
            return;
        }

        try {
            QueryDetailResponseDTO detailedData = apiQueryService.doSearchContract(vehicle.placa);

            if (detailedData != null && detailedData.success() && detailedData.data() != null) {
                if (detailedData.data().contrato() != null &&
                        detailedData.data().contrato().protocolo() != null) {
                    vehicle.protocolo = detailedData.data().contrato().protocolo();
                }

                if (detailedData.data().credor() != null &&
                        detailedData.data().credor().endereco() != null) {
                    vehicle.cidade = extractCityFromAddress(detailedData.data().credor().endereco());
                }

                if (detailedData.data().devedores() != null &&
                        !detailedData.data().devedores().isEmpty()) {
                    vehicle.cpfDevedor = detailedData.data().devedores().get(0).cpfCnpj();
                }

                log.trace("Dados detalhados obtidos para veículo: placa={}", vehicle.placa);
            }

        } catch (Exception e) {
            log.warn("Erro ao buscar dados detalhados do veículo {}: {}", vehicle.placa, e.getMessage());
        }
    }

    private VehicleDTO convertToVehicleDTO(VehicleCompleteData data) {
        return new VehicleDTO(
                null,
                data.credor,
                data.dataPedido,
                cryptoService.encryptContrato(data.contrato),
                cryptoService.encryptPlaca(data.placa),
                data.modelo,
                data.uf,
                cryptoService.encryptCidade(data.cidade),
                cryptoService.encryptCpfDevedor(data.cpfDevedor),
                cryptoService.encryptProtocolo(data.protocolo),
                data.etapaAtual,
                data.statusApreensao,
                data.ultimaMovimentacao
        );
    }

    private <T> List<List<T>> createBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    private String extractContrato(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.numeroContrato() != null) return notification.numeroContrato();
        if (notification.contrato() != null && !notification.contrato().isEmpty()) {
            return notification.contrato().get(0).numero();
        }
        return "N/A";
    }

    private String extractPlaca(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.veiculos() != null && !notification.veiculos().isEmpty()) {
            return notification.veiculos().get(0).placa();
        }
        return "N/A";
    }

    private String extractCredor(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.nomeCredor() != null) return notification.nomeCredor();
        if (notification.credor() != null && !notification.credor().isEmpty()) {
            return notification.credor().get(0).nome();
        }
        return "N/A";
    }

    private LocalDate extractDataPedido(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.dataPedido() != null) {
            try {
                return LocalDate.parse(notification.dataPedido().substring(0, 10));
            } catch (Exception e) {
            }
        }
        return null;
    }

    private String extractModelo(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.veiculos() != null && !notification.veiculos().isEmpty()) {
            return notification.veiculos().get(0).modelo();
        }
        return "N/A";
    }

    private String extractUF(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.veiculos() != null && !notification.veiculos().isEmpty()) {
            return notification.veiculos().get(0).ufEmplacamento();
        }
        return "N/A";
    }

    private String extractEtapa(ConsultaNotificationResponseDTO.NotificationData notification) {
        return notification.etapa() != null ? notification.etapa() : "A iniciar";
    }

    private String extractStatus(ConsultaNotificationResponseDTO.NotificationData notification) {
        return "Aguardando Agendamento da Diligência";
    }

    private LocalDateTime extractMovimentacao(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.dataMovimentacao() != null) {
            try {
                return LocalDateTime.parse(notification.dataMovimentacao().replace(" ", "T"));
            } catch (Exception e) {
            }
        }
        return LocalDateTime.now();
    }

    private String extractProtocoloBasic(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.protocolo() != null) return notification.protocolo();
        if (notification.contrato() != null && !notification.contrato().isEmpty()) {
            String proto = notification.contrato().get(0).protocolo();
            return proto != null ? proto : "N/A";
        }
        return "N/A";
    }

    private String extractCpfBasic(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.devedor() != null && !notification.devedor().isEmpty()) {
            return notification.devedor().get(0).cpfCnpj();
        }
        return "N/A";
    }

    private String extractCityFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) return "N/A";

        String clean = address.replaceAll("\\d{5}-?\\d{3}", "").replaceAll("(?i)cep:.*", "").trim();
        String[] parts = clean.split(" - ");

        if (parts.length >= 2) {
            String city = parts[parts.length - 2].trim();
            if (!city.isEmpty() && !city.matches("[A-Z]{2}")) {
                return city;
            }
        }

        return "N/A";
    }

    private String generateKey(String contrato, String placa) {
        return contrato + "|" + placa;
    }

    private static class VehicleCompleteData {
        String contrato;
        String placa;
        String credor;
        LocalDate dataPedido;
        String modelo;
        String uf;
        String cidade;
        String cpfDevedor;
        String protocolo;
        String etapaAtual;
        String statusApreensao;
        LocalDateTime ultimaMovimentacao;
    }
}
