package com.montreal.oauth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.oauth.domain.dto.RoleDTO;
import com.montreal.oauth.domain.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles do Sistema", description = "API para gerenciamento de roles, incluindo permissões e funcionalidades.")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @Operation(
        summary = "Lista todas as roles",
        description = "Recupera todas as roles cadastradas no sistema, incluindo suas permissões e funcionalidades."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de roles recuperada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Buscando todas as roles.");
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(
        summary = "Busca uma role por ID",
        description = "Recupera os detalhes de uma role específica com base no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role encontrada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(
            @Parameter(description = "ID da role a ser buscada", required = true)
            @PathVariable Integer id) {
        log.info("Buscando role com ID: {}", id);
        try {
            RoleDTO roleDTO = roleService.getRoleById(id);
            return ResponseEntity.ok(roleDTO);
        } catch (Exception e) {
            log.error("Erro ao buscar role com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Cria uma nova role",
        description = "Adiciona uma nova role ao sistema, incluindo suas permissões e funcionalidades associadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role criada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping("/create")
    public ResponseEntity<RoleDTO> createRole(
            @Parameter(description = "Objeto contendo os dados da nova role", required = true)
            @Valid @RequestBody RoleDTO roleDTO) {
        try {
            log.info("Criando nova role: {}", roleDTO.getName());
            RoleDTO createdRole = roleService.createRole(roleDTO);
            return ResponseEntity.ok(createdRole);
        } catch (Exception e) {
            log.error("Erro ao criar role: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Atualiza permissões de uma role",
        description = "Modifica as permissões associadas a uma role existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissões atualizadas com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleDTO> updatePermissions(
            @Parameter(description = "ID da role cujas permissões serão atualizadas", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Lista de IDs das permissões a serem associadas", required = true)
            @RequestBody List<Long> permissionIds) {
        try {
            log.info("Atualizando permissões da role ID: {}", id);
            RoleDTO updatedRole = roleService.updateRolePermissions(id, permissionIds);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            log.error("Erro ao atualizar permissões da role ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Atualiza funcionalidades de uma role",
        description = "Modifica as funcionalidades associadas a uma role existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Funcionalidades atualizadas com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/{id}/functionalities")
    public ResponseEntity<RoleDTO> updateFunctionalities(
            @Parameter(description = "ID da role cujas funcionalidades serão atualizadas", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Lista de IDs das funcionalidades a serem associadas", required = true)
            @RequestBody List<Long> functionalityIds) {
        try {
            log.info("Atualizando funcionalidades da role ID: {}", id);
            RoleDTO updatedRole = roleService.updateRoleFunctionalities(id, functionalityIds);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            log.error("Erro ao atualizar funcionalidades da role ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Exclui uma role",
        description = "Remove uma role do sistema permanentemente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Role não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "ID da role a ser excluída", required = true)
            @PathVariable Integer id) {
        try {
            log.info("Deletando role ID: {}", id);
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erro ao deletar role ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
