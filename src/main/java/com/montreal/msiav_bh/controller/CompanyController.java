package com.montreal.msiav_bh.controller;

import com.montreal.core.domain.exception.ConflictException;
import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.request.CompanyRequest;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.dto.response.CompanyTypeResponse;
import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;
import com.montreal.msiav_bh.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company")
@Tag(name = "Empresas", description = "Operações para gerenciamento de empresas")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Criar uma nova empresa")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CompanyRequest companyRequest) {
        try {
            log.info("Criando nova empresa: {}", companyRequest);
            CompanyResponse createdCompany = companyService.save(companyRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
        } catch (InvalidParameterException | ConflictException e) {
            log.warn("Erro de validação ou conflito ao criar empresa: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao criar empresa: {}", companyRequest, e);
            throw new InternalErrorException("Erro inesperado ao criar empresa.", e);
        }
    }

    @Operation(summary = "Buscar empresa por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            log.info("Buscando empresa com ID: {}", id);
            CompanyResponse company = companyService.findById(id);
            if (company == null) {
                throw new NotFoundException("Empresa não encontrada com ID: " + id);
            }
            return ResponseEntity.ok(company);
        } catch (NotFoundException e) {
            log.warn("Empresa não encontrada com ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresa por ID: {}", id, e);
            throw new InternalErrorException("Erro inesperado ao buscar empresa.", e);
        }
    }

    @Operation(summary = "Atualizar uma empresa")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CompanyRequest companyRequest) {
        try {
            log.info("Atualizando empresa com ID {}: {}", id, companyRequest);
            CompanyResponse updatedCompany = companyService.update(companyRequest, id);
            return ResponseEntity.ok(updatedCompany);
        } catch (InvalidParameterException | ConflictException | NotFoundException e) {
            log.warn("Erro ao atualizar empresa com ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar empresa com ID {}: {}", id, companyRequest, e);
            throw new InternalErrorException("Erro inesperado ao atualizar empresa.", e);
        }
    }

    @Operation(summary = "Deletar empresa por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            log.info("Deletando empresa com ID: {}", id);
            companyService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            log.warn("Empresa com ID {} não encontrada para exclusão: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar empresa com ID {}", id, e);
            throw new InternalErrorException("Erro inesperado ao deletar empresa.", e);
        }
    }

    @Operation(summary = "Buscar todas as empresas sem paginação")
    @GetMapping("/all")
    public ResponseEntity<?> findAll() {
        try {
            log.info("Buscando todas as empresas");
            List<CompanyResponse> companies = companyService.findAll();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar todas as empresas", e);
            throw new InternalErrorException("Erro inesperado ao buscar todas as empresas.", e);
        }
    }

    @Operation(summary = "Listar todos os tipos de empresas")
    @GetMapping("/types")
    public ResponseEntity<?> listCompanyTypes() {
        try {
            log.info("Listando todos os tipos de empresas");
            List<CompanyTypeResponse> companyTypes = Arrays.stream(CompanyTypeEnum.values())
                    .map(type -> new CompanyTypeResponse(type.getCode(), type.getDescription()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(companyTypes);
        } catch (Exception e) {
            log.error("Erro inesperado ao listar tipos de empresas", e);
            throw new InternalErrorException("Erro inesperado ao listar tipos de empresas.", e);
        }
    }

    @Operation(summary = "Criar empresa no PostgreSQL")
    @PostMapping("/postgrees-company")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyRequest companyRequest) {
        try {
            log.info("Criando empresa no PostgreSQL: {}", companyRequest);
            CompanyResponse created = companyService.createCompany(companyRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (InvalidParameterException | ConflictException e) {
            log.warn("Erro de validação ou conflito ao criar empresa no PostgreSQL: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao criar empresa no PostgreSQL: {}", companyRequest, e);
            throw new InternalErrorException("Erro inesperado ao criar empresa no PostgreSQL.", e);
        }
    }

    @Operation(summary = "Buscar empresas com filtros dinâmicos e paginação")
    @GetMapping("/search")
    public ResponseEntity<?> searchWithFilters(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {
            log.info("Iniciando busca com filtros: {}", filters);

            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

            Page<CompanyResponse> results = companyService.searchWithFilters(filters, pageable);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Erro ao buscar empresas com filtros dinâmicos", e);
            throw new InternalErrorException("Erro inesperado ao buscar empresas com filtros dinâmicos.", e);
        }
    }
    
}
