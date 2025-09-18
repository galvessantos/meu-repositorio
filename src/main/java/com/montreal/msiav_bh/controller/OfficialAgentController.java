package com.montreal.msiav_bh.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.request.SimpleUserVehicleAssociationRequest;
import com.montreal.msiav_bh.dto.response.UserVehicleAssociationResponse;
import com.montreal.msiav_bh.dto.response.VehicleResponse;
import com.montreal.msiav_bh.service.OfficialAgentService;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.UserInfo;
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
@RequestMapping("/api/v1/official-agent")
@Tag(name = "Agente Oficial", description = "Operações para gerenciamento de vínculo entre agente oficial e veículos")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class OfficialAgentController {

    private final OfficialAgentService officialAgentService;

    @Operation(summary = "Verificar se é agente oficial")
    @GetMapping("/check/{idUser}")
    public ResponseEntity<Boolean> isOfficialAgent(@PathVariable Long idUser) {
        log.info("Verificando se o usuário com id {} é agente oficial", idUser);
        try {
            return ResponseEntity.ok(officialAgentService.isAgenteOficial(idUser));
        } catch (Exception e) {
            log.error("Erro ao verificar agente oficial para usuário {}: {}", idUser, e.getMessage(), e);
            throw new BusinessException("Erro ao verificar agente oficial.", e);
        }
    }

    @Operation(summary = "Buscar veículos do agente oficial")
    @GetMapping("/vehicles/{idUser}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByOfficialAgent(@PathVariable Long idUser) {
        log.info("Buscando veículos do agente oficial com ID {}", idUser);
        try {
            List<VehicleResponse> vehicles = officialAgentService.veiculosDoAgenteOficial(idUser);
            if (vehicles.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(vehicles);
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar veículos do agente oficial", e);
            throw new BusinessException("Erro ao buscar veículos do agente oficial", e);
        }
    }

    @Operation(summary = "Listar agentes oficiais de uma empresa")
    @GetMapping("/company/{companyId}/official-agent")
    public ResponseEntity<List<UserResponse>> getOfficialAgentsByCompany(@PathVariable Long companyId) {
        log.info("Listando agentes oficiais da empresa {}", companyId);
        try {
            List<UserInfo> agentes = officialAgentService.usuariosDaEmpresa(companyId);
            if (agentes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(officialAgentService.mapResponseAgenteOficialWithoutDecrypt(agentes));
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar agentes oficiais da empresa {}", companyId, e);
            throw new BusinessException("Erro ao buscar agentes oficiais da empresa", e);
        }
    }

    @Operation(summary = "Buscar agente oficial do tipo DADOS_DETRAN por veículo")
    @GetMapping("/agente-dados-detran/{vehicleId}")
    public ResponseEntity<UserResponse> getDetranOfficialAgentByVehicle(@PathVariable Long vehicleId) {
        log.info("Buscando agente oficial DADOS_DETRAN para veículo {}", vehicleId);
        try {
            List<UserInfo> agentes = officialAgentService.findAgentesByVehicleIdAndType(vehicleId);
            if (agentes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            UserInfo ultimoAgente = agentes.get(agentes.size() - 1);
            return ResponseEntity.ok(officialAgentService.mapResponseAgenteOficial2(ultimoAgente));
        } catch (InvalidParameterException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar agente DADOS_DETRAN para veículo {}", vehicleId, e);
            throw new BusinessException("Erro ao buscar agente oficial do tipo DADOS_DETRAN para o veículo", e);
        }
    }

    @Operation(summary = "Vincular agente oficial a um veículo")
    @PostMapping("/associate")
    public ResponseEntity<UserVehicleAssociationResponse> associateOfficialAgentToVehicle(@Valid @RequestBody SimpleUserVehicleAssociationRequest request) {
        log.info("Iniciando vínculo do agente oficial ID={} ao veículo ID={}", request.getUserId(), request.getVehicleId());
        try {
            var response = officialAgentService.vincularVeiculoAgenteOficial(request.getVehicleId(), request.getUserId());
            return ResponseEntity.ok(response);
        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro ao vincular agente oficial: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao vincular agente oficial", e);
            throw new InternalErrorException("Erro ao processar vínculo entre agente oficial e veículo.");
        }
    }

}
