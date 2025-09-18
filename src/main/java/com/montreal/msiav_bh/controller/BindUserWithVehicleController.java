package com.montreal.msiav_bh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.ErrorHandlerUtil;
import com.montreal.msiav_bh.service.BindUserWithVehicleService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Veículo - Usuário", description = "Operações para gerenciamento de vínculo entre veículo e usuário")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class BindUserWithVehicleController {

    private final BindUserWithVehicleService bindUserWithVehicleService;

    @PostMapping("/bind/agente-oficial/company")
    public ResponseEntity<?> bindUserAgenteOficialWithVehicleByCompany(
            @RequestParam Long companyId,
            @RequestParam Long vehicleId
    ) {
        log.info("Iniciando vínculo de usuários da empresa ID={} com veículo ID={}", companyId, vehicleId);
        try {
            bindUserWithVehicleService.bindUserAgenteOficialWithVehicleByCompany(companyId, vehicleId);
            return ResponseEntity.ok("Vínculo criado com sucesso para usuários da empresa com o veículo.");
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar vínculo entre empresa e veículo", e);
            return ErrorHandlerUtil.handleError("Erro ao criar vínculo entre empresa e veículo", e);
        }
    }

    @PostMapping("/bind/agente-oficial")
    public ResponseEntity<?> bindUserWithVehicle(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam Long associatedBy
    ) {
        log.info("Iniciando vínculo do usuário ID={} com veículo ID={}, associado por {}", userId, vehicleId, associatedBy);
        try {
            String message = bindUserWithVehicleService.bindUserWithVehicleAgenteOficial(userId, vehicleId, associatedBy);
            return ResponseEntity.ok(message);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao vincular usuário ao veículo", e);
            return ErrorHandlerUtil.handleError("Erro ao vincular usuário ao veículo", e);
        }
    }

    @DeleteMapping("/unbind/agente-oficial")
    public ResponseEntity<?> unbindUserWithVehicle(
            @RequestParam Long userId,
            @RequestParam Long vehicleId
    ) {
        log.info("Removendo vínculo do usuário ID={} com veículo ID={}", userId, vehicleId);
        try {
            String message = bindUserWithVehicleService.unbindUserWithVehicleAgenteOficial(userId, vehicleId);
            return ResponseEntity.ok(message);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao remover vínculo entre usuário e veículo", e);
            return ErrorHandlerUtil.handleError("Erro ao remover vínculo entre usuário e veículo", e);
        }
    }
}
