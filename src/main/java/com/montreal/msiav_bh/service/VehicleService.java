package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.PageDTO;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO;
import com.montreal.msiav_bh.dto.response.VehicleResponse;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.mapper.VehicleInquiryMapper;
import com.montreal.msiav_bh.mapper.VehicleMapper;
import com.montreal.msiav_bh.repository.VehicleRepository;
import com.montreal.msiav_bh.utils.exceptions.ValidationMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);
    private final VehicleRepository vehicleRepository;

    public Vehicle findById(Long vehicleId) {
        log.info("Buscando veiculo na base local com id: {}", vehicleId);
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> new RuntimeException("Veiculo não encontrado"));
    }

    public Vehicle findByContractNumber(String contractNumber) {
        log.info("Buscando veiculo na base local com contrato: {}", contractNumber);
        return vehicleRepository.findByContractNumber(contractNumber).orElseThrow(() -> new RuntimeException("Veiculo não encontrado"));
    }

    public VehicleResponse findVehicleById(Long vehicleId) {
        log.info("Buscando veiculo na base local com ID: {}", vehicleId);
        var vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new RuntimeException("Veiculo com ID " + vehicleId + " não foi encontrado"));
        return VehicleMapper.INSTANCE.toVehicleResponse(vehicle);
    }

    public Page<Vehicle> searchWithFilters(String fieldName, String fieldValue, Pageable pageable) {
        log.info("Buscando veículos pelo campo {} com valor: {}", fieldName, fieldValue);
        if (fieldName == null || fieldValue == null || fieldValue.isBlank()) {
            log.info("Nenhum filtro informado, retornando todos os veículos.");
            return vehicleRepository.findAll(pageable);
        }
        return vehicleRepository.searchByDynamicField(fieldName, fieldValue, pageable);
    }

    @Autowired
    private ApiQueryService apiQueryService;

    @Autowired
    private VehicleInquiryMapper mapper;

    public PageDTO<VehicleDTO> buscarComFiltros(
            LocalDate dataInicio, LocalDate dataFim, String credor, String contrato,
            String protocolo, String cpf, String uf, String cidade, String modelo,
            String placa, String etapaAtual, String statusApreensao,
            int page, int size, String sortBy, String sortDir) {

        try {
            List<com.montreal.msiav_bh.dto.response.ConsultaNotificationResponseDTO.NotificationData> notifications;

            if (dataInicio != null && dataFim != null) {
                notifications = apiQueryService.searchByPeriod(dataInicio, dataFim);
                List<VehicleDTO> veiculos = mapper.mapToVeiculoDTOForResponse(notifications);
                veiculos = aplicarFiltrosLocais(veiculos, credor, contrato, protocolo,
                        cpf, uf, cidade, modelo, placa,
                        etapaAtual, statusApreensao);
                return paginar(ordenar(veiculos, sortBy, sortDir), page, size);
            }
//            else if (contrato != null || placa != null) {
//                if (credor == null && uf == null && modelo == null && etapaAtual == null) {
//                    throw new IllegalArgumentException(ValidationMessages.CONTRATO_PLACA_SOZINHO);
//                }
//
//                LocalDate endDate = LocalDate.now();
//                LocalDate startDate = endDate.minusDays(90);
//                notifications = apiQueryService.searchByPeriod(startDate, endDate);
//
//                List<VehicleDTO> vehicle = mapper.mapToVeiculoDTO(notifications);
//                vehicle = aplicarFiltrosLocais(vehicle, credor, contrato, protocolo,
//                        cpf, uf, cidade, modelo, placa,
//                        etapaAtual, statusApreensao);
//                return paginar(ordenar(vehicle, sortBy, sortDir), page, size);
//            }
//            else if (credor != null || uf != null || modelo != null || etapaAtual != null) {
//                throw new IllegalArgumentException(ValidationMessages.COMBINACAO_INVALIDA);
//            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            notifications = apiQueryService.searchByPeriod(startDate, endDate);
            List<VehicleDTO> veiculos = mapper.mapToVeiculoDTOForResponse(notifications);
            veiculos = aplicarFiltrosLocais(veiculos, credor, contrato, protocolo,
                    cpf, uf, cidade, modelo, placa, etapaAtual, statusApreensao);
            return paginar(ordenar(veiculos, sortBy, sortDir), page, size);



        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Falha na busca: {}", e.getMessage());
            throw new RuntimeException(ValidationMessages.FILTRO_COMBINACAO_INVALIDA, e);
        }
    }

    private List<VehicleDTO> aplicarFiltrosLocais(List<VehicleDTO> veiculos,
                                                  String credor, String contrato, String protocolo,
                                                  String cpf, String uf, String cidade,
                                                  String modelo, String placa,
                                                  String etapaAtual, String statusApreensao) {
        return veiculos.stream()
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
                .collect(Collectors.toList());
    }

    public PageDTO<VehicleDTO> buscarTodos(int page, int size, String sortBy, String sortDir) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(120);


            logger.info("Buscando veículos de {} a {}", startDate, endDate);
            System.out.println("Buscando veículos de {} a {}"+ startDate + endDate);

            List<ConsultaNotificationResponseDTO.NotificationData> notifications =
                    apiQueryService.searchByPeriod(startDate, endDate);

            if (notifications == null || notifications.isEmpty()) {
                logger.error("Nenhum dado recebido da API Montreal");
                return PageDTO.of(List.of(), page, size, 0);
            }

            logger.debug("Dados crus recebidos: {}", notifications);

            List<VehicleDTO> veiculos = mapper.mapToVeiculoDTOForResponse(notifications);

            if (veiculos.isEmpty()) {
                logger.warn("Mapeamento retornou vazio para {} notificações", notifications.size());
            }

            return paginar(ordenar(veiculos, sortBy, sortDir), page, size);
        } catch (Exception e) {
            logger.error("Falha completa na busca: ", e);
            throw new RuntimeException("Erro ao buscar veículos: " + e.getMessage());
        }
    }

    public void healthCheck() {
        apiQueryService.authenticate();
    }

    private PageDTO<VehicleDTO> paginar(List<VehicleDTO> veiculos, int page, int size) {
        int total = veiculos.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        return start >= total
                ? PageDTO.of(List.of(), page, size, total)
                : PageDTO.of(veiculos.subList(start, end), page, size, total);
    }

    private List<VehicleDTO> ordenar(List<VehicleDTO> veiculos, String sortBy, String sortDir) {
        Comparator<VehicleDTO> comparator = switch (sortBy.toLowerCase()) {
            case "credor" -> Comparator.comparing(VehicleDTO::credor, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "datapedido" -> Comparator.comparing(VehicleDTO::dataPedido, Comparator.nullsLast(LocalDate::compareTo));
            case "placa" -> Comparator.comparing(VehicleDTO::placa, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> Comparator.comparing(VehicleDTO::protocolo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        };
        return veiculos.stream()
                .sorted("desc".equalsIgnoreCase(sortDir) ? comparator.reversed() : comparator)
                .collect(Collectors.toList());
    }
}