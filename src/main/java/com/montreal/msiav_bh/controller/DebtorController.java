package com.montreal.msiav_bh.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.ErrorHandlerUtil;
import com.montreal.msiav_bh.dto.request.DebtorRequest;
import com.montreal.msiav_bh.dto.response.DebtorResponse;
import com.montreal.msiav_bh.entity.Debtor;
import com.montreal.msiav_bh.service.DebtorService;
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
@RequestMapping("/api/v1/debtors")
@Tag(name = "Devedor", description = "Operações para gerenciamento do devedor")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class DebtorController {

    private final DebtorService debtorService;

    @PostMapping
    public ResponseEntity<DebtorResponse> createDebtor(@Valid @RequestBody DebtorRequest debtorRequest) {
        try {
            DebtorResponse response = debtorService.createDebtor(debtorRequest);
            return ResponseEntity.ok(response);
        } catch (BusinessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping
    public ResponseEntity<List<DebtorResponse>> getAllDebtors() {
        List<DebtorResponse> responses = debtorService.getAllDebtors();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtorResponse> getDebtorById(@PathVariable Long id) {
        try {
            DebtorResponse response = debtorService.getDebtorById(id);
            return ResponseEntity.ok(response);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtorResponse> updateDebtor(
            @PathVariable Long id,
            @Valid @RequestBody DebtorRequest debtorRequest) {
        try {
            DebtorResponse updatedDebtor = debtorService.updateDebtor(id, debtorRequest);
            return ResponseEntity.ok(updatedDebtor);
        } catch (BusinessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
    
    @Operation(summary = "Buscar devedores com filtros dinâmicos e paginação")
    @GetMapping("/search")
    public ResponseEntity<?> searchWithFilters(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        try {
            log.info("Iniciando busca de devedores - Field: {}, Value: {}, Page: {}, Size: {}, Sort: {}", 
                     field, value, page, size, Arrays.toString(sort));

            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("desc") 
                                       ? Sort.Direction.DESC 
                                       : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

            Page<Debtor> results = debtorService.searchWithFilters(field, value, pageable);

            log.info("Busca concluída. Total de devedores encontrados: {}", results.getTotalElements());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ErrorHandlerUtil.handleError("Erro ao buscar devedores.", e);
        }
    }
    
}

