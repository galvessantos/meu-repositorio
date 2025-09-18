package com.montreal.msiav_bh.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.request.AddressRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.mapper.AddressMapper;
import com.montreal.msiav_bh.repository.AddressRepository;
import com.montreal.oauth.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper mapper;
    private final AddressRepository addressRepository;

    @Transactional
    public AddressResponse save(AddressRequest request) {
        log.info("Verificando se o endereço já existe no banco antes de salvar: {}", request);
        try {
            Optional<Address> existingAddress = addressRepository.findByPostalCodeAndStreetAndNumberAndNeighborhoodAndCity(
                    request.getPostalCode(),
                    request.getStreet(),
                    request.getNumber(),
                    request.getNeighborhood(),
                    request.getCity()
            );

            if (existingAddress.isPresent()) {
                log.info("Endereço já existente. ID: {}", existingAddress.get().getId());
                return mapper.toResponse(existingAddress.get());
            }

            Address address = mapper.toEntity(request);
            Address savedAddress = addressRepository.save(address);
            log.info("Endereço criado com sucesso. ID: {}", savedAddress.getId());
            return mapper.toResponse(savedAddress);

        } catch (Exception e) {
            log.error("Erro ao salvar endereço: {}", request, e);
            throw new InternalErrorException("Erro ao salvar o endereço. Tente novamente.");
        }
    }

    public AddressResponse findById(Long id) {
        try {
            log.info("Buscando endereço por ID: {}", id);
            Address address = addressRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Endereço não encontrado com ID: " + id));
            return mapper.toResponse(address);
        } catch (Exception e) {
            log.error("Erro ao buscar endereço por ID {}", id, e);
            throw new InternalErrorException("Erro ao buscar endereço por ID " + id);
        }
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        log.info("Iniciando atualização do endereço com ID {}: {}", id, request);
        try {
            Optional<Address> existingAddressOpt = addressRepository.findById(id);
            if (existingAddressOpt.isEmpty()) {
                log.warn("Endereço com ID {} não encontrado para atualização.", id);
                throw new NotFoundException("Endereço não encontrado.");
            }

            Address existingAddress = existingAddressOpt.get();
            mapper.updateEntityFromRequest(request, existingAddress);
            Address updatedAddress = addressRepository.save(existingAddress);
            
            log.info("Endereço atualizado com sucesso. ID: {}", updatedAddress.getId());
            return mapper.toResponse(updatedAddress);
        } catch (Exception e) {
            log.error("Erro ao atualizar endereço com ID {}: {}", id, request, e);
            throw new InternalErrorException("Erro ao atualizar o endereço. Tente novamente.");
        }
    }

    public void delete(Long id) {
        try {
            log.info("Deletando endereço com ID: {}", id);
            if (!addressRepository.existsById(id)) {
                throw new NotFoundException("Endereço não encontrado com ID: " + id);
            }
            addressRepository.deleteById(id);
            log.info("Endereço deletado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao deletar endereço com ID {}", id, e);
            throw new InternalErrorException("Erro ao deletar endereço com ID " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAll() {
        log.info("Recuperando todos os endereços do banco de dados.");
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(mapper::toResponse).toList();
    }

    public void validateAddressIdExists(Long companyId) {
        try {
            log.info("Validando se o endereço com ID {} existe.", companyId);
            boolean exists = addressRepository.existsById(companyId);
            if (!exists) {
                log.error("Endereço com ID {} não encontrado.", companyId);
                throw new BusinessException(String.format("Endereço com ID %s não encontrado.", companyId), "ADDRESS_NOT_FOUND");
            }
        } catch (Exception e) {
            log.error("Erro ao validar existência do endereço com ID {}", companyId, e);
            throw new InternalErrorException("Erro ao validar existência do endereço com ID " + companyId);
        }
    }
    
    public Page<AddressResponse> searchWithFilters(String fieldName, String fieldValue, Pageable pageable) {
        log.info("Buscando endereços pelo campo {} com valor: {}", fieldName, fieldValue);
        if (fieldName == null || fieldValue == null || fieldValue.isBlank()) {
            log.info("Nenhum filtro informado, retornando todos os endereços.");
            return addressRepository.findAll(pageable).map(mapper::toResponse);
        }
        Page<Address> addresses =  addressRepository.searchByDynamicField(fieldName, fieldValue, pageable);
        return addresses.map(mapper::toResponse);
    }

}
