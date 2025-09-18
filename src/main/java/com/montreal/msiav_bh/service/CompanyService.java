package com.montreal.msiav_bh.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.montreal.core.domain.exception.ConflictException;
import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.request.CompanyRequest;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.entity.Company;
import com.montreal.msiav_bh.mapper.AddressMapper;
import com.montreal.msiav_bh.mapper.CompanyMapper;
import com.montreal.msiav_bh.repository.AddressRepository;
import com.montreal.msiav_bh.repository.CompanyPRepository;
import com.montreal.msiav_bh.repository.CompanyRepository;
import com.montreal.oauth.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

	private final ModelMapper modelMapper;
    private final CompanyMapper companyMapper;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;
    private final CompanyPRepository companyPRepository;

    public CompanyResponse save(CompanyRequest request) {
        log.info("Salvando empresa: {}", request);

        if (!StringUtils.hasText(request.getName())) {
            throw new InvalidParameterException("O nome da empresa é obrigatório.");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new InvalidParameterException("O e-mail da empresa é obrigatório.");
        }
        if (!StringUtils.hasText(request.getDocument())) {
            throw new InvalidParameterException("O documento da empresa é obrigatório.");
        }
        if (request.getCompanyType() == null) {
            throw new InvalidParameterException("O tipo da empresa é obrigatório.");
        }
        if (request.getIsActive() == null) {
            throw new InvalidParameterException("O estado de atividade da empresa é obrigatório.");
        }
        if (request.getAddress() == null) {
            throw new InvalidParameterException("O endereço da empresa é obrigatório.");
        }

        // Verificações de conflito
        if (companyRepository.existsByEmail(request.getEmail())) {
            log.warn("Empresa com email '{}' já cadastrada.", request.getEmail());
            throw new ConflictException("Já existe uma empresa cadastrada com o e-mail informado.");
        }

        if (companyRepository.existsByDocument(request.getDocument())) {
            log.warn("Empresa com documento '{}' já cadastrada.", request.getDocument());
            throw new ConflictException("Já existe uma empresa cadastrada com o documento informado.");
        }

        try {
            Company entity = companyMapper.toEntity(request);
            entity.setCreatedAt(LocalDateTime.now());

            Company savedEntity = companyRepository.save(entity);
            log.info("Empresa salva com sucesso. ID: {}", savedEntity.getId());

            return companyMapper.toResponse(savedEntity);

        } catch (Exception e) {
            log.error("Erro inesperado ao salvar empresa: {}", request, e);
            throw new BusinessException("Erro inesperado ao salvar empresa.", e);
        }
    }

    @Transactional
    public CompanyResponse update(CompanyRequest dto, Long id) {
        log.info("Atualizando empresa com ID {}: {}", id, dto);

        Company existingEntity = companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Empresa não encontrada com ID: " + id));

        try {
            if (StringUtils.hasText(dto.getName())) {
                existingEntity.setName(dto.getName());
            }
            if (StringUtils.hasText(dto.getEmail())) {
                existingEntity.setEmail(dto.getEmail());
            }
            if (StringUtils.hasText(dto.getDocument())) {
                existingEntity.setDocument(dto.getDocument());
            }
            if (StringUtils.hasText(dto.getNameResponsible())) {
                existingEntity.setNameResponsible(dto.getNameResponsible());
            }
            if (dto.getCompanyType() != null) {
                existingEntity.setCompanyType(dto.getCompanyType());
            }

            if (dto.getAddress() != null) {
                Address incomingAddress = addressMapper.toEntity(dto.getAddress());

                Address addressToUse = addressRepository.findExistingAddress(
                        incomingAddress.getPostalCode(),
                        incomingAddress.getStreet(),
                        incomingAddress.getNumber(),
                        incomingAddress.getNeighborhood(),
                        incomingAddress.getCity()
                ).orElseGet(() -> addressRepository.save(incomingAddress));

                existingEntity.setAddress(addressToUse);
            }

            existingEntity.setIsActive(dto.getIsActive());

            Company updatedEntity = companyRepository.save(existingEntity);
            log.info("Empresa atualizada com sucesso. ID: {}", updatedEntity.getId());
            return companyMapper.toResponse(updatedEntity);

        } catch (Exception e) {
            log.error("Erro ao atualizar empresa com ID {}: {}", id, dto, e);
            throw new InternalErrorException("Erro inesperado ao atualizar empresa.", e);
        }
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        log.info("Buscando empresa com ID: {}", id);
        try {
            Company company = companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Empresa não encontrada com ID: " + id));
            log.info("Empresa encontrada com sucesso. ID: {}", company.getId());
            return companyMapper.toResponse(company);
        } catch (NotFoundException e) {
            log.warn("Empresa com ID {} não encontrada: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresa com ID: {}", id, e);
            throw new InternalErrorException("Erro inesperado ao buscar empresa.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        log.info("Buscando todas as empresas sem paginação");
        try {
            List<Company> companies = companyRepository.findAll();
            log.info("Total de empresas encontradas: {}", companies.size());
            return companies.stream()
                    .map(companyMapper::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar todas as empresas", e);
            throw new InternalErrorException("Erro inesperado ao buscar todas as empresas.", e);
        }
    }

    public void validateCompanyIdExists(Long vehicleId) {
        log.info("Validando se a empresa com ID {} existe.", vehicleId);
        if (!companyRepository.existsById(vehicleId)) {
            log.error("Empresa com ID {} não encontrado.", vehicleId);
            throw new NotFoundException(String.format("Empresa %s não encontrado.", vehicleId));
        }
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        log.info("Iniciando criação de empresa: {}", request);
        try {
            Company company = modelMapper.map(request, Company.class);
            Company saved = companyPRepository.save(company);
            log.info("Empresa criada com sucesso. ID: {}", saved.getId());
            return modelMapper.map(saved, CompanyResponse.class);
        } catch (Exception e) {
            log.error("Erro ao criar empresa: {}", request, e);
            throw new InternalErrorException("Erro inesperado ao criar empresa.", e);
        }
    }

    public List<CompanyResponse> getAllCompanies() {
        log.info("Iniciando busca de todas as empresas no PostgreSQL");
        try {
            List<Company> companies = companyPRepository.findAll();
            log.info("Total de empresas encontradas no PostgreSQL: {}", companies.size());
            return companies.stream()
                    .map(company -> modelMapper.map(company, CompanyResponse.class))
                    .toList();
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresas no PostgreSQL", e);
            throw new InternalErrorException("Erro inesperado ao buscar empresas no PostgreSQL.", e);
        }
    }
    
    public CompanyResponse getCompanyById(Long id) {
        log.info("Buscando empresa com ID: {}", id);
        try {
            Company company = companyPRepository.findById(id).orElseThrow(() -> new NotFoundException("Empresa não encontrada com ID: " + id));
            log.info("Empresa encontrada com sucesso. ID: {}", company.getId());
            return modelMapper.map(company, CompanyResponse.class);
        } catch (NotFoundException e) {
            log.warn("Empresa com ID {} não encontrada: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresa com ID: {}", id, e);
            throw new InternalErrorException("Erro inesperado ao buscar empresa.", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        log.info("Iniciando exclusão da empresa com ID: {}", id);
        try {
            if (!companyPRepository.existsById(id)) {
                log.warn("Empresa com ID {} não encontrada para exclusão.", id);
                throw new NotFoundException("Empresa não encontrada com ID: " + id);
            }
            companyPRepository.deleteById(id);
            log.info("Empresa com ID {} excluída com sucesso.", id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao excluir empresa com ID: {}", id, e);
            throw new InternalErrorException("Erro inesperado ao excluir empresa.", e);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchWithFilters(Map<String, String> filters, Pageable pageable) {
        log.info("Buscando empresas com filtros dinâmicos: {}", filters);

        // lista os campos que pode filtrar
        List<String> validFields = List.of(
            "name", "email", "document", "companyType", "isActive", "nameResponsible", "address.city"
        );

        if (filters.containsKey("city")) {
            filters.put("address.city", filters.remove("city"));
        }

        Specification<Company> specification = Specification.where(null);

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();

            if (!validFields.contains(field)) {
                log.warn("Campo de filtro inválido: {}", field);
                continue; // ignora campos inválidos
            }

            specification = specification.and((root, query, cb) -> {
                if (field.contains(".")) {
                    String[] parts = field.split("\\.");
                    return cb.like(
                        cb.lower(root.join(parts[0]).get(parts[1]).as(String.class)),
                        "%" + value.toLowerCase() + "%"
                    );
                } else {
                    return cb.like(
                        cb.lower(root.get(field).as(String.class)),
                        "%" + value.toLowerCase() + "%"
                    );
                }
            });
        }

        Page<Company> result = companyRepository.findAll(specification, pageable);
        return result.map(companyMapper::toResponse);
    }

}
