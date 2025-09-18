package com.montreal.msiav_bh.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.request.ProbableAddressRequest;
import com.montreal.msiav_bh.dto.response.AddressResponse;
import com.montreal.msiav_bh.dto.response.ProbableAddressResponse;
import com.montreal.msiav_bh.service.ProbableAddressService;
import com.montreal.oauth.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/probable-address")
@Tag(name = "Endereço Provável", description = "Operações para gerenciamento de endereços prováveis dos veículos")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class ProbableAddressController {

    private final ProbableAddressService probableAddressService;

    @Operation(summary = "Salvar endereço provável do veículo")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody ProbableAddressRequest request) {
        log.info("Iniciando salvamento do endereço provável para veículo ID: {}", request.getVehicleId());
        try {
            ProbableAddressResponse response = probableAddressService.save(request);
            log.info("Endereço provável salvo com sucesso para veículo ID: {}", request.getVehicleId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação ao salvar endereço provável: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar endereço provável", e);
            throw new BusinessException("Erro ao salvar endereço provável", e);
        }
    }

    @Operation(summary = "Buscar endereço provável por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        log.info("Buscando endereço provável com ID: {}", id);
        try {
            ProbableAddressResponse response = probableAddressService.findById(id);
            return ResponseEntity.ok(response);
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação ao buscar endereço provável: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar endereço provável com ID: {}", id, e);
            throw new BusinessException("Erro ao buscar endereço provável", e);
        }
    }
    
    @Operation(summary = "Buscar endereços prováveis por ID do veículo")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> findByVehicleId(@PathVariable Long vehicleId) {
        log.info("Buscando endereços prováveis para o veículo ID: {}", vehicleId);
        try {
            List<ProbableAddressResponse> list = probableAddressService.findByVehicleId(vehicleId);
            return ResponseEntity.ok(list);
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação ao buscar endereços prováveis: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar endereços prováveis para o veículo ID: {}", vehicleId, e);
            throw new BusinessException("Erro ao buscar endereços prováveis", e);
        }
    }

    @Operation(summary = "Listar todos os endereços prováveis")
    @GetMapping
    public ResponseEntity<?> findAll() {
        log.info("Listando todos os endereços prováveis");
        try {
            List<ProbableAddressResponse> list = probableAddressService.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Erro inesperado ao listar endereços prováveis", e);
            throw new BusinessException("Erro ao listar endereços prováveis", e);
        }
    }

    @Operation(summary = "Atualizar endereço provável por ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ProbableAddressRequest request) {
        log.info("Atualizando endereço provável com ID: {}", id);
        try {
            ProbableAddressResponse response = probableAddressService.update(id, request);
            log.info("Endereço provável com ID {} atualizado com sucesso", id);
            return ResponseEntity.ok(response);
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação ao atualizar endereço provável: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar endereço provável com ID: {}", id, e);
            throw new BusinessException("Erro ao atualizar endereço provável", e);
        }
    }
    
    @Operation(summary = "Excluir endereço provável por ID do endereço e ID do veículo")
    @DeleteMapping("/{addressId}/{vehicleId}")
    public ResponseEntity<?> deleteByAddressIdAndVehicleId(
            @PathVariable Long addressId,
            @PathVariable Long vehicleId) {

        log.info("Deletando endereço provável com addressId: {} e vehicleId: {}", addressId, vehicleId);
        try {
            probableAddressService.deleteByVehicleIdAndAddressId(vehicleId, addressId);
            return ResponseEntity.noContent().build();
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação ao deletar endereço provável: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar endereço provável", e);
            throw new BusinessException("Erro ao deletar endereço provável", e);
        }
    }


}
