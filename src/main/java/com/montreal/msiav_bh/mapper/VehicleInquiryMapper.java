package com.montreal.msiav_bh.mapper;

import com.montreal.core.utils.PostgresCryptoUtil;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO;
import com.montreal.msiav_bh.repository.QueryResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public class VehicleInquiryMapper {

    private static final Logger logger = LoggerFactory.getLogger(VehicleInquiryMapper.class);

    private final PostgresCryptoUtil cryptoUtil;
    private final QueryResultRepository queryResultRepository;

    public VehicleInquiryMapper(PostgresCryptoUtil cryptoUtil, QueryResultRepository queryResultRepository) {
        this.cryptoUtil = cryptoUtil;
        this.queryResultRepository = queryResultRepository;
    }

    public List<VehicleDTO> mapToVeiculoDTO(List<ConsultaNotificationResponseDTO.NotificationData> notifications) {
        return mapToVeiculoDTO(notifications, true);
    }

    public List<VehicleDTO> mapToVeiculoDTOForResponse(List<ConsultaNotificationResponseDTO.NotificationData> notifications) {
        return mapToVeiculoDTO(notifications, false);
    }

    private List<VehicleDTO> mapToVeiculoDTO(List<ConsultaNotificationResponseDTO.NotificationData> notifications, boolean shouldEncrypt) {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        logger.info("Processando {} notificações (encrypt={})", notifications.size(), shouldEncrypt);

        Map<String, VehicleDTO> veiculosMap = new LinkedHashMap<>();

        for (ConsultaNotificationResponseDTO.NotificationData notification : notifications) {
            if (notification == null) continue;

            if (notification.veiculos() != null && !notification.veiculos().isEmpty()) {
                logger.debug("Processando {} veículos da notificação", notification.veiculos().size());
                for (ConsultaNotificationResponseDTO.VeiculoInfo veiculo : notification.veiculos()) {
                    VehicleDTO dto = createVeiculoDTOWithVehicleInfo(notification, veiculo, shouldEncrypt);
                    if (dto != null) {
                        String uniqueKey = generateUniqueKey(dto);
                        veiculosMap.merge(uniqueKey, dto, this::selectMostRecentVehicle);
                    }
                }
            } else {
                logger.debug("Processando dados gerais da notificação");
                VehicleDTO dto = createVeiculoDTOFromNotification(notification, shouldEncrypt);
                if (dto != null) {
                    String uniqueKey = generateUniqueKey(dto);
                    veiculosMap.merge(uniqueKey, dto, this::selectMostRecentVehicle);
                }
            }
        }

        logger.info("Processamento concluído: {} veículos únicos de {} notificações",
                veiculosMap.size(), notifications.size());

        return new ArrayList<>(veiculosMap.values());
    }

    private String generateUniqueKey(VehicleDTO dto) {
        StringBuilder keyBuilder = new StringBuilder();

        if (dto.contrato() != null && !"N/A".equals(dto.contrato()) && !dto.contrato().trim().isEmpty()) {
            String contrato = dto.contrato().trim();
            if (contrato.length() >= 50 && contrato.matches("[a-fA-F0-9]+")) {
                keyBuilder.append("contrato_hash:").append(contrato.hashCode());
            } else {
                keyBuilder.append("contrato:").append(contrato);
            }
        }

        if (dto.placa() != null && !"N/A".equals(dto.placa()) && !dto.placa().trim().isEmpty()) {
            String placa = dto.placa().trim();
            if (placa.length() >= 50 && placa.matches("[a-fA-F0-9]+")) {
                keyBuilder.append("|placa_hash:").append(placa.hashCode());
            } else {
                keyBuilder.append("|placa:").append(placa);
            }
        }

        if (dto.credor() != null && !dto.credor().trim().isEmpty()) {
            keyBuilder.append("|credor:").append(dto.credor().trim());
        }

        if (dto.modelo() != null && !"N/A".equals(dto.modelo()) && !dto.modelo().trim().isEmpty()) {
            keyBuilder.append("|modelo:").append(dto.modelo().trim());
        }

        if (dto.uf() != null && !"N/A".equals(dto.uf()) && !dto.uf().trim().isEmpty()) {
            keyBuilder.append("|uf:").append(dto.uf().trim());
        }

        if (keyBuilder.isEmpty() || keyBuilder.toString().length() < 10) {
            if (dto.dataPedido() != null) {
                keyBuilder.append("data:").append(dto.dataPedido());
            }
        }

        if (keyBuilder.isEmpty()) {
            keyBuilder.append("hash:").append(Math.abs(dto.hashCode()));
        }

        String uniqueKey = keyBuilder.toString();
        logger.trace("Chave única gerada: {}", uniqueKey);
        return uniqueKey;
    }

    private VehicleDTO selectMostRecentVehicle(VehicleDTO existing, VehicleDTO novo) {
        logger.debug("Realizando merge de veículos duplicados");

        if (existing == null) {
            return novo;
        }
        if (novo == null) {
            return existing;
        }

        VehicleDTO selected = selectByMovementDate(existing, novo);
        if (selected != null) {
            return mergeVehicleData(existing, novo, selected);
        }

        selected = selectByRequestDate(existing, novo);
        if (selected != null) {
            return mergeVehicleData(existing, novo, selected);
        }

        selected = selectByDataCompleteness(existing, novo);
        return mergeVehicleData(existing, novo, selected);
    }

    private VehicleDTO selectByMovementDate(VehicleDTO existing, VehicleDTO novo) {
        LocalDateTime existingMovement = existing.ultimaMovimentacao();
        LocalDateTime novoMovement = novo.ultimaMovimentacao();

        if (existingMovement != null && novoMovement != null) {
            if (existingMovement.isEqual(novoMovement)) {
                return selectByDataCompleteness(existing, novo);
            }
            VehicleDTO selected = novoMovement.isAfter(existingMovement) ? novo : existing;
            logger.debug("Merge baseado em ultimaMovimentacao: {} vs {} -> selecionado {}",
                    novoMovement, existingMovement,
                    selected == novo ? "novo" : "existing");
            return selected;
        }

        if (novoMovement != null) {
            logger.debug("Merge: novo registro tem ultimaMovimentacao, existing não");
            return novo;
        }
        if (existingMovement != null) {
            logger.debug("Merge: existing tem ultimaMovimentacao, novo não");
            return existing;
        }

        return null;
    }

    private VehicleDTO selectByRequestDate(VehicleDTO existing, VehicleDTO novo) {
        LocalDate existingRequest = existing.dataPedido();
        LocalDate novoRequest = novo.dataPedido();

        if (existingRequest != null && novoRequest != null) {
            if (existingRequest.isEqual(novoRequest)) {
                return selectByDataCompleteness(existing, novo);
            }
            VehicleDTO selected = novoRequest.isAfter(existingRequest) ? novo : existing;
            logger.debug("Merge baseado em dataPedido: {} vs {} -> selecionado {}",
                    novoRequest, existingRequest,
                    selected == novo ? "novo" : "existing");
            return selected;
        }

        if (novoRequest != null) {
            logger.debug("Merge: novo registro tem dataPedido, existing não");
            return novo;
        }
        if (existingRequest != null) {
            logger.debug("Merge: existing tem dataPedido, novo não");
            return existing;
        }

        return null;
    }

    private VehicleDTO selectByDataCompleteness(VehicleDTO existing, VehicleDTO novo) {
        int existingScore = calculateCompletenessScore(existing);
        int novoScore = calculateCompletenessScore(novo);

        logger.debug("Score de completude - existing: {}, novo: {}", existingScore, novoScore);

        if (novoScore > existingScore) {
            logger.debug("Merge: novo registro tem mais dados completos");
            return novo;
        } else if (existingScore > novoScore) {
            logger.debug("Merge: existing tem mais dados completos");
            return existing;
        } else {
            logger.debug("Merge: empate na completude, mantendo novo registro");
            return novo;
        }
    }

    private int calculateCompletenessScore(VehicleDTO vehicle) {
        int score = 0;

        if (isValueValid(vehicle.contrato())) score += 3;
        if (isValueValid(vehicle.placa())) score += 3;
        if (isValueValid(vehicle.protocolo())) score += 2;
        if (vehicle.dataPedido() != null) score += 2;
        if (vehicle.ultimaMovimentacao() != null) score += 2;

        if (isValueValid(vehicle.credor())) score += 1;
        if (isValueValid(vehicle.modelo())) score += 1;
        if (isValueValid(vehicle.uf())) score += 1;
        if (isValueValid(vehicle.cidade())) score += 1;
        if (isValueValid(vehicle.cpfDevedor())) score += 1;
        if (isValueValid(vehicle.etapaAtual())) score += 1;
        if (isValueValid(vehicle.statusApreensao())) score += 1;

        return score;
    }

    private VehicleDTO mergeVehicleData(VehicleDTO existing, VehicleDTO novo, VehicleDTO base) {
        if (base == null) {
            logger.warn("Base para merge é null, retornando novo");
            return novo;
        }

        VehicleDTO other = (base == novo) ? existing : novo;

        return new VehicleDTO(
                mergeField(base.id(), other.id()),
                mergeStringField(base.credor(), other.credor()),
                mergeField(base.dataPedido(), other.dataPedido()),
                mergeStringField(base.contrato(), other.contrato()),
                mergeStringField(base.placa(), other.placa()),
                mergeStringField(base.modelo(), other.modelo()),
                mergeStringField(base.uf(), other.uf()),
                mergeStringField(base.cidade(), other.cidade()),
                mergeStringField(base.cpfDevedor(), other.cpfDevedor()),
                mergeStringField(base.protocolo(), other.protocolo()),
                mergeStringField(base.etapaAtual(), other.etapaAtual()),
                mergeStringField(base.statusApreensao(), other.statusApreensao()),
                mergeField(base.ultimaMovimentacao(), other.ultimaMovimentacao())
        );
    }

    private String mergeStringField(String baseValue, String otherValue) {
        if (isValueValid(baseValue)) {
            return baseValue;
        }

        if (isValueValid(otherValue)) {
            return otherValue;
        }

        if (baseValue != null) {
            return baseValue;
        }

        return otherValue;
    }

    private <T> T mergeField(T baseValue, T otherValue) {
        if (baseValue != null) {
            return baseValue;
        }
        return otherValue;
    }

    private boolean isValueValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmed = value.trim();

        return !("N/A".equalsIgnoreCase(trimmed) || "null".equalsIgnoreCase(trimmed) || "undefined".equalsIgnoreCase(trimmed) || "-".equals(trimmed));
    }

    private void logMergeDecision(VehicleDTO existing, VehicleDTO novo, VehicleDTO selected, String reason) {
        if (logger.isDebugEnabled()) {
            logger.debug("MERGE DECISION:");
            logger.debug("  Existing: contrato={}, placa={}, ultimaMov={}",
                    maskSensitiveData(existing.contrato()),
                    maskSensitiveData(existing.placa()),
                    existing.ultimaMovimentacao());
            logger.debug("  Novo:     contrato={}, placa={}, ultimaMov={}",
                    maskSensitiveData(novo.contrato()),
                    maskSensitiveData(novo.placa()),
                    novo.ultimaMovimentacao());
            logger.debug("  Selected: {} (razão: {})",
                    selected == novo ? "NOVO" : "EXISTING", reason);
        }
    }

    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "***";
        }
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }

    private VehicleDTO createVeiculoDTOFromNotification(ConsultaNotificationResponseDTO.NotificationData notification, boolean shouldEncrypt) {
        try {
            String credor = notification.nomeCredor() != null ? notification.nomeCredor() :
                    (notification.credor() != null && !notification.credor().isEmpty() ?
                            notification.credor().get(0).nome() : "N/A");

            String contrato = notification.numeroContrato() != null ? notification.numeroContrato() :
                    (notification.contrato() != null && !notification.contrato().isEmpty() ?
                            notification.contrato().get(0).numero() : "N/A");

            String protocolo = notification.protocolo() != null ? notification.protocolo() :
                    (notification.contrato() != null && !notification.contrato().isEmpty() ?
                            notification.contrato().get(0).protocolo() : "N/A");

            LocalDateTime dataPedidoDateTime = parseDateTime(notification.dataPedido());
            LocalDateTime dataMovimentacao = parseDateTime(notification.dataMovimentacao());

            if (notification.contrato() != null && !notification.contrato().isEmpty()) {
                var contratoInfo = notification.contrato().get(0);
                if (dataPedidoDateTime == null) dataPedidoDateTime = parseContractDate(contratoInfo.dataPedido());
                if (dataMovimentacao == null) dataMovimentacao = parseContractDate(contratoInfo.dataNotificacao());
            }

            LocalDate dataPedido = dataPedidoDateTime != null ? dataPedidoDateTime.toLocalDate() : null;

            String cpfDevedor = notification.devedor() != null && !notification.devedor().isEmpty() ?
                    notification.devedor().get(0).cpfCnpj() : "N/A";

            String placa = "N/A";
            String modelo = "N/A";
            String uf = "N/A";

            if (notification.veiculos() != null && !notification.veiculos().isEmpty()) {
                var veiculo = notification.veiculos().get(0);
                placa = veiculo.placa() != null ? veiculo.placa() : "N/A";
                modelo = veiculo.modelo() != null ? veiculo.modelo() : "N/A";
                uf = veiculo.ufEmplacamento() != null ? veiculo.ufEmplacamento() : "N/A";
            }

            String cidade = extractCityFromNotification(notification);
            String etapaAtual = notification.etapa() != null ? notification.etapa() : determineEtapaAtual(notification);
            String statusApreensao = determineStatusApreensao(notification);

            String placaCriptografada = criptografarSeDiferente(placa, shouldEncrypt);
            String cpfCriptografado = criptografarSeDiferente(cpfDevedor, shouldEncrypt);
            String contratoCriptografado = criptografarSeDiferente(contrato, shouldEncrypt);

            return new VehicleDTO(null, credor, dataPedido, contratoCriptografado,
                    placaCriptografada, modelo, uf, cidade, cpfCriptografado, protocolo,
                    etapaAtual, statusApreensao, dataMovimentacao);

        } catch (Exception e) {
            logger.error("Erro ao criar VehicleDTO a partir da notificação", e);
            return null;
        }
    }

    private VehicleDTO createVeiculoDTOWithVehicleInfo(ConsultaNotificationResponseDTO.NotificationData notification,
                                                       ConsultaNotificationResponseDTO.VeiculoInfo veiculo, boolean shouldEncrypt) {
        try {
            String credor = notification.nomeCredor() != null ? notification.nomeCredor() :
                    (notification.credor() != null && !notification.credor().isEmpty() ?
                            notification.credor().get(0).nome() : "N/A");

            String contrato = notification.numeroContrato() != null ? notification.numeroContrato() :
                    (notification.contrato() != null && !notification.contrato().isEmpty() ?
                            notification.contrato().get(0).numero() : "N/A");

            String protocolo = notification.protocolo() != null ? notification.protocolo() :
                    (notification.contrato() != null && !notification.contrato().isEmpty() ?
                            notification.contrato().get(0).protocolo() : "N/A");

            LocalDateTime dataPedidoDateTime = parseDateTime(notification.dataPedido());
            LocalDateTime dataMovimentacao = parseDateTime(notification.dataMovimentacao());

            if (notification.contrato() != null && !notification.contrato().isEmpty()) {
                var contratoInfo = notification.contrato().get(0);
                if (dataPedidoDateTime == null) dataPedidoDateTime = parseContractDate(contratoInfo.dataPedido());
                if (dataMovimentacao == null) dataMovimentacao = parseContractDate(contratoInfo.dataNotificacao());
            }

            LocalDate dataPedido = dataPedidoDateTime != null ? dataPedidoDateTime.toLocalDate() : null;

            String cpfDevedor = notification.devedor() != null && !notification.devedor().isEmpty() ?
                    notification.devedor().get(0).cpfCnpj() : "N/A";

            String cidade = extractCityFromNotification(notification);
            String etapaAtual = notification.etapa() != null ? notification.etapa() : determineEtapaAtual(notification);
            String statusApreensao = determineStatusApreensaoFromVeiculo(veiculo);

            String placaCriptografada = criptografarSeDiferente(veiculo.placa(), shouldEncrypt);
            String cpfCriptografado = criptografarSeDiferente(cpfDevedor, shouldEncrypt);
            String contratoCriptografado = criptografarSeDiferente(contrato, shouldEncrypt);

            return new VehicleDTO(null, credor, dataPedido, contratoCriptografado,
                    placaCriptografada,
                    veiculo.modelo() != null ? veiculo.modelo() : "N/A",
                    veiculo.ufEmplacamento() != null ? veiculo.ufEmplacamento() : "N/A",
                    cidade, cpfCriptografado, protocolo, etapaAtual, statusApreensao, dataMovimentacao);

        } catch (Exception e) {
            logger.error("Erro ao criar VehicleDTO com informações do veículo", e);
            return null;
        }
    }

    private String criptografarSeDiferente(String valor, boolean shouldEncrypt) {
        if (valor == null || "N/A".equals(valor) || valor.trim().isEmpty()) {
            return valor;
        }

        if (!shouldEncrypt) {
            return valor;
        }

        if (isAlreadyEncrypted(valor)) {
            logger.debug("Valor já está criptografado, retornando sem modificar");
            return valor;
        }

        try {
            String encrypted = cryptoUtil.encrypt(valor);
            logger.debug("Valor criptografado com sucesso: {} chars", encrypted != null ? encrypted.length() : 0);
            return encrypted;
        } catch (Exception e) {
            logger.warn("Erro ao criptografar valor [{}]: {}", valor.length(), e.getMessage());
            return valor;
        }
    }

    private boolean isAlreadyEncrypted(String value) {
        return value != null && value.length() >= 50 && value.matches("[a-fA-F0-9]+");
    }

    private String extractCityFromNotification(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.devedor() != null && !notification.devedor().isEmpty()) {
            var devedor = notification.devedor().get(0);
            if (devedor.enderecos() != null && !devedor.enderecos().isEmpty()) {
                for (var endereco : devedor.enderecos()) {
                    var cidade = extractCityFromAddress(endereco.endereco());
                    if (!"N/A".equals(cidade)) return cidade;
                }
            }
        }

        if (notification.contrato() != null && !notification.contrato().isEmpty()) {
            var contrato = notification.contrato().get(0);
            if (contrato.municipioContrato() != null && !contrato.municipioContrato().trim().isEmpty()) {
                return contrato.municipioContrato();
            }
        }

        return "N/A";
    }

    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;

        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                try {
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return date.atStartOfDay();
                } catch (DateTimeParseException e3) {
                    logger.debug("Erro ao fazer parse da data: {}", dateString);
                    return null;
                }
            }
        }
    }

    private LocalDateTime parseContractDate(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay();
    }

    private String extractCityFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) return "N/A";

        String cleanAddress = address.replaceAll("\\d{5}-?\\d{3}", "").trim();
        String[] separators = {",", "-", "–", "/"};

        for (String separator : separators) {
            if (cleanAddress.contains(separator)) {
                String[] parts = cleanAddress.split(separator);
                for (int i = parts.length - 1; i >= 0; i--) {
                    String part = parts[i].trim();
                    if (part.length() > 2 && !part.matches("\\d+") && !part.matches("[A-Z]{2}")) {
                        part = part.replaceAll("\\d+", "").trim();
                        if (part.length() > 2) return part;
                    }
                }
            }
        }

        String[] words = cleanAddress.replaceAll("\\d+", " ").trim().split("\\s+");
        if (words.length >= 2) {
            String possibleCity = words[words.length - 2];
            if (possibleCity.length() > 2 && !possibleCity.matches("[A-Z]{2}")) {
                return possibleCity;
            }
        }

        return "N/A";
    }

    private String determineEtapaAtual(ConsultaNotificationResponseDTO.NotificationData notification) {
        if (notification.etapa() != null && !notification.etapa().trim().isEmpty()) {
            return notification.etapa();
        }

        if (notification.contrato() != null && !notification.contrato().isEmpty()) {
            var contrato = notification.contrato().get(0);
            if (contrato.dataCertidao() != null) return "Certidão de Busca Apreensão Emitida";
            if (contrato.dataPedido() != null) return "Pedido Registrado";
        }

        return "A iniciar";
    }

    private String determineStatusApreensao(ConsultaNotificationResponseDTO.NotificationData notification) {

        return "Aguardando Agendamento da Diligência";
    }

    private String determineStatusApreensaoFromVeiculo(ConsultaNotificationResponseDTO.VeiculoInfo veiculo) {
        if ("S".equalsIgnoreCase(veiculo.possuiGps())) return "Localizador Acionado";
        if (veiculo.gravame() != null && !veiculo.gravame().trim().isEmpty()) {
            return "Guincho acionado";
        }
        if (veiculo.registroDetran() != null && !veiculo.registroDetran().trim().isEmpty()) {
            return "Localizador Acionado";
        }
        return "Aguardando Agendamento da Diligência";
    }

    private boolean hasOrgaoAcionado(ConsultaNotificationResponseDTO.NotificationData notification) {
        return notification.veiculos() != null && !notification.veiculos().isEmpty()
                && notification.contrato() != null && !notification.contrato().isEmpty()
                && notification.devedor() != null && !notification.devedor().isEmpty();
    }
}