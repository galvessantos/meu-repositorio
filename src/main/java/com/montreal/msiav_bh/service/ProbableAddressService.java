package com.montreal.msiav_bh.service;

import java.util.List;
import java.util.Optional;

import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.repository.VehicleCacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.ValidationUtil;
import com.montreal.msiav_bh.dto.request.ProbableAddressRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.dto.response.ProbableAddressResponse;
import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.entity.ProbableAddress;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.mapper.AddressMapper;
import com.montreal.msiav_bh.mapper.ProbableAddressMapper;
import com.montreal.msiav_bh.repository.AddressRepository;
import com.montreal.msiav_bh.repository.ProbableAddressRepository;
import com.montreal.msiav_bh.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProbableAddressService {

    private final ProbableAddressMapper mapper;
    private final AddressMapper addressMapper;
    private final VehicleCacheRepository vehicleRepository;
    private final ProbableAddressRepository repository;
    private final AddressRepository addressRepository;

    @Transactional
    public ProbableAddressResponse save(ProbableAddressRequest request) {
        try {
            log.info("Salvando endereço provável para veículo ID: {}", request.getVehicleId());

            VehicleCache vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new NotFoundException("Veículo não encontrado com ID: " + request.getVehicleId()));

            Address newAddress = addressMapper.toEntity(request.getAddress());

            // Validação do endereço
            ValidationUtil.validate(newAddress);

            // Buscar se já existe o endereço
            Optional<Address> optionalAddress = addressRepository.findByPostalCodeAndStreetAndNumberAndNeighborhoodAndCity(
                    newAddress.getPostalCode(),
                    newAddress.getStreet(),
                    newAddress.getNumber(),
                    newAddress.getNeighborhood(),
                    newAddress.getCity()
            );

            Address addressToUse = optionalAddress.orElseGet(() -> addressRepository.save(newAddress));

            // Verifica se já existe ProbableAddress
            Optional<ProbableAddress> probableAddressOptional = existsProbableAddress(addressToUse.getId(), vehicle.getId());
            if (probableAddressOptional.isPresent()) {
                log.info("Endereço provável já existente para veículo ID: {}", request.getVehicleId());
                return mapper.toResponse(probableAddressOptional.get());
            }

            ProbableAddress entity = ProbableAddress.builder()
                    .vehicle(vehicle)
                    .address(addressToUse)
                    .build();

            ProbableAddress saved = repository.save(entity);
            log.info("Endereço provável salvo com ID: {}", saved.getId());

            return mapper.toResponse(saved);

        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro conhecido ao salvar endereço provável: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar endereço provável", e);
            throw new InternalErrorException("Erro ao salvar endereço provável", e);
        }
    }

    private Optional<ProbableAddress> existsProbableAddress(Long addressId, Long vehicleId) {
        return repository.findByVehicleIdAndAddressId(vehicleId, addressId);
    }


    public ProbableAddressResponse findById(Long id) {
        try {
            log.info("Buscando endereço provável com ID: {}", id);
            ProbableAddress entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço provável não encontrado com ID: " + id));
            return mapper.toResponse(entity);
        } catch (Exception e) {
            log.error("Erro ao buscar endereço provável com ID: {}", id, e);
            throw new InternalErrorException("Erro ao buscar endereço provável", e);
        }
    }

    public List<ProbableAddressResponse> findAll() {
        try {
            log.info("Listando todos os endereços prováveis");
            return repository.findAll().stream()
                    .map(mapper::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Erro ao listar endereços prováveis", e);
            throw new InternalErrorException("Erro ao listar endereços prováveis", e);
        }
    }

    @Transactional
    public ProbableAddressResponse update(Long id, ProbableAddressRequest request) {
        try {
            log.info("Atualizando endereço provável com ID: {}", id);
            ProbableAddress existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço provável não encontrado com ID: " + id));

            Address updatedAddress = addressMapper.toEntity(request.getAddress());
            existing.setAddress(updatedAddress);

            ProbableAddress saved = repository.save(existing);
            log.info("Endereço provável atualizado com ID: {}", saved.getId());
            return mapper.toResponse(saved);
        } catch (Exception e) {
            log.error("Erro ao atualizar endereço provável com ID: {}", id, e);
            throw new InternalErrorException("Erro ao atualizar endereço provável", e);
        }
    }

    public List<ProbableAddressResponse> findByVehicleId(Long vehicleId) {
        try {
            log.info("Buscando endereços para o veículo ID: {}", vehicleId);

            return repository.findAllByVehicleId(vehicleId).stream()
                    .map(pa -> ProbableAddressResponse.builder()
                            .id(pa.getId()) // id do ProbableAddress
                            .vehicleId(pa.getVehicle().getId()) // id do veículo
                            .address(addressMapper.toResponse(pa.getAddress())) // dto do endereço
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao buscar endereços DTO para o veículo ID {}: {}", vehicleId, e);
            throw new InternalErrorException("Erro ao buscar endereços prováveis do veículo.");
        }
    }


    @Transactional
    public void deleteByVehicleIdAndAddressId(Long vehicleId, Long addressId) {
        try {
            log.info("Tentando excluir endereço provável com vehicleId: {} e addressId: {}", vehicleId, addressId);

            ProbableAddress probableAddress = repository.findByVehicleIdAndAddressId(vehicleId, addressId)
                    .orElseThrow(() -> new NotFoundException(String.format("Endereço provável não encontrado para o veículo %d e endereço %d.", vehicleId, addressId)));

            repository.delete(probableAddress);
            log.info("Endereço provável deletado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao deletar endereço provável", e);
            throw new InternalErrorException("Erro ao deletar endereço provável", e);
        }
    }


}