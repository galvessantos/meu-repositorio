package com.montreal.msiav_bh.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.HistoryRequest;
import com.montreal.msiav_bh.dto.response.HistoryResponse;
import com.montreal.msiav_bh.entity.History;
import com.montreal.msiav_bh.mapper.HistoryMapper;
import com.montreal.msiav_bh.repository.HistoryRepository;
import com.montreal.msiav_bh.repository.VehicleRepository;
import com.montreal.oauth.exception.BusinessException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class HistoryService {

	private final HistoryMapper historyMapper;
    private final VehicleRepository vehicleRepository;
    private final HistoryRepository historyRepository;

    public HistoryResponse save(HistoryRequest request) {
        try {
            log.info("Iniciando salvamento de histórico para veículo ID={}", request.getIdVehicle());
            validateFields(request);

            var vehicle = vehicleRepository.findById(request.getIdVehicle())
                    .orElseThrow(() -> new NotFoundException("Veículo não encontrado com ID: " + request.getIdVehicle()));

            request.setModel(vehicle.getModel());
            request.setLicensePlate(vehicle.getLicensePlate());
            request.setContractNumber(vehicle.getContractNumber());
            request.setCreditorName(vehicle.getCreditorName());

            if (request.getCreationDateTime() == null) {
                request.setCreationDateTime(LocalDateTime.now());
            }

            var entity = historyMapper.toEntity(request);
            var saved = historyRepository.save(entity);

            log.info("Histórico salvo com sucesso. ID={}", saved.getId());
            return historyMapper.toResponse(saved);

        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar histórico", e);
            throw new BusinessException("Erro ao salvar histórico.", e);
        }
    }

    public HistoryResponse update(HistoryRequest request, Long id) {
        validateFields(request);
        verifyVehicleExists(request.getIdVehicle());
        History existingHistory = historyRepository.findById(id).orElseThrow(() -> new NotFoundException("Histórico não encontrado com ID: " + id));
        historyMapper.updateEntityFromRequest(request, existingHistory);
        History updatedHistory = historyRepository.save(existingHistory);
        return historyMapper.toResponse(updatedHistory);
    }

    private void validateFields(HistoryRequest request) {
        if (request.getIdVehicle() == null) {
            throw new InvalidParameterException("idVehicle não pode ser nulo ou vazio");
        }
        if (request.getLicensePlate() == null || request.getLicensePlate().trim().isEmpty()) {
            throw new InvalidParameterException("licensePlate não pode ser nulo ou vazio");
        }
    }

    private void verifyVehicleExists(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new NotFoundException("Veículo não encontrado com ID: " + vehicleId);
        }
    }

    public List<HistoryRequest> listAllByIdVehicle(Long vehicleId) {
        log.info("Listando todos os históricos para o ID do veículo {}", vehicleId);

        if (vehicleId == null) {
            throw new InvalidParameterException("O ID do veículo não pode ser nulo ou vazio");
        }

        verifyVehicleExists(vehicleId);

        List<History> histories = historyRepository.findByIdVehicle(vehicleId);

        if (histories.isEmpty()) {
            throw new NotFoundException("Nenhum histórico encontrado para o ID do veículo: " + vehicleId);
        }

        return histories.stream().map(historyMapper::toDto).collect(Collectors.toList());
    }
    
    public Page<HistoryResponse> listAllByIdVehiclePage(Long vehicleId, Pageable pageable) {
        try {
            log.info("Listando históricos paginados para veículo ID={}", vehicleId);

            if (vehicleId == null) {
                throw new InvalidParameterException("O ID do veículo não pode ser nulo ou vazio");
            }

            verifyVehicleExists(vehicleId);

            Page<History> page = historyRepository.findByVehicleId(vehicleId, pageable);
            if (page.isEmpty()) {
                throw new NotFoundException("Nenhum histórico encontrado para o veículo ID: " + vehicleId);
            }

            List<HistoryResponse> responses = page.getContent()
                    .stream().map(historyMapper::toResponse).collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, page.getTotalElements());

        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao listar históricos paginados do veículo ID={}", vehicleId, e);
            throw new BusinessException("Erro ao listar históricos paginados.", e);
        }
    }
    
    public HistoryResponse getById(Long id) {
        try {
            if (id == null || id <= 0) {
                log.warn("ID inválido: {}", id);
                throw new InvalidParameterException("O ID informado é inválido.");
            }

            return historyRepository.findById(id)
                    .map(historyMapper::toResponse)
                    .orElseThrow(() -> {
                        log.warn("Histórico não encontrado para o ID {}", id);
                        return new NotFoundException("Histórico não encontrado com ID: " + id);
                    });

        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar histórico com ID {}", id, e);
            throw new BusinessException("Erro interno ao buscar histórico com ID: " + id, e);
        }
    }

    
}
