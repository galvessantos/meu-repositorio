package com.montreal.msiav_bh.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.ErrorHandlerUtil;
import com.montreal.msiav_bh.dto.request.AddressRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.service.AddressService;
import com.montreal.oauth.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Operações para gerenciamento de endereços")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Criar um novo endereço")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AddressRequest addressRequest) {
        try {
            log.info("Criando novo endereço: {}", addressRequest);
            AddressResponse response = addressService.save(addressRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar endereço", e);
            return ErrorHandlerUtil.handleError("Erro ao criar endereço.", e);
        }
    }

    @Operation(summary = "Buscar endereço por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            log.info("Buscando endereço por ID: {}", id);
            AddressResponse response = addressService.findById(id);
            return ResponseEntity.ok(response);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereço por ID", e);
            return ErrorHandlerUtil.handleError("Erro ao buscar endereço.", e);
        }
    }

    @Operation(summary = "Atualizar um endereço existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody AddressRequest addressRequest, @PathVariable Long id) {
        try {
            log.info("Atualizando endereço com ID {}: {}", id, addressRequest);
            AddressResponse response = addressService.update(id, addressRequest);
            return ResponseEntity.ok(response);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar endereço", e);
            return ErrorHandlerUtil.handleError("Erro ao atualizar endereço.", e);
        }
    }

    @Operation(summary = "Deletar um endereço por ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        try {
            log.info("Deletando endereço com ID: {}", id);
            addressService.delete(id);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao deletar endereço", e);
            throw new BusinessException("Erro ao deletar endereço", e);
        }
    }

    @Operation(summary = "Buscar todos os endereços sem paginação")
    @GetMapping("/all")
    public ResponseEntity<?> findAll() {
        try {
            log.info("Buscando todos os endereços.");
            List<AddressResponse> addresses = addressService.findAll();
            log.info("Total de endereços encontrados: {}", addresses.size());
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Erro ao buscar endereços", e);
            return ErrorHandlerUtil.handleError("Erro ao buscar endereços.", e);
        }
    }

    @Operation(summary = "Buscar endereços com filtros dinâmicos e paginação")
    @GetMapping("/search")
    public ResponseEntity<?> searchWithFilters(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {
            log.info("Iniciando busca com filtros dinâmicos - Field: {}, Value: {}, Page: {}, Size: {}, Sort: {}",
                    field, value, page, size, sort);

            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

            Page<AddressResponse> results = addressService.searchWithFilters(field, value, pageable);
            log.info("Busca concluída com sucesso. Total de resultados: {}", results.getTotalElements());
            return ResponseEntity.ok(results);

        } catch (InvalidParameterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereços com filtros", e);
            return ErrorHandlerUtil.handleError("Erro ao buscar endereços com filtros dinâmicos.", e);
        }
    }
}
