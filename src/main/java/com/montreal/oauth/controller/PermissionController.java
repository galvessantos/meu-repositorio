package com.montreal.oauth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.montreal.oauth.domain.dto.PermissionDTO;
import com.montreal.oauth.domain.dto.request.RolePermissionsRequest;
import com.montreal.oauth.domain.dto.response.PermissionResponseDTO;
import com.montreal.oauth.domain.entity.Permission;
import com.montreal.oauth.domain.mapper.PermissionMapper;
import com.montreal.oauth.domain.service.PermissionService;

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

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissões", description = "API para gerenciamento de permissões no sistema.")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @Operation(
        summary = "Lista todas as permissões",
        description = "Retorna uma lista com todas as permissões disponíveis no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de permissões retornada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> findAll() {
        log.info("Buscando todas as permissões.");
        List<PermissionResponseDTO> permissions = permissionMapper.toResponseDTOList(permissionService.findAll());
        return ResponseEntity.ok(permissions);
    }

    @Operation(
        summary = "Busca permissão por ação e sujeito",
        description = "Retorna uma permissão específica com base na ação e no sujeito (entidade associada)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissão encontrada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Permissão não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/action/{action}/subject/{subject}")
    public ResponseEntity<PermissionResponseDTO> findByActionAndSubject(
            @Parameter(description = "Ação da permissão (ex: READ, WRITE, DELETE)", required = true)
            @PathVariable String action,
            @Parameter(description = "Sujeito da permissão (ex: User, Vehicle, Company)", required = true)
            @PathVariable String subject) {
        log.info("Buscando permissão para ação '{}' e sujeito '{}'.", action, subject);
        PermissionResponseDTO responseDTO = permissionService.findByActionAndSubject(action, subject);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Cria uma nova permissão",
        description = "Adiciona uma nova permissão ao sistema, associando-a a uma ação e um sujeito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Permissão criada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<PermissionResponseDTO> create(
            @Parameter(description = "Objeto contendo os dados da nova permissão", required = true)
            @Valid @RequestBody PermissionDTO dto) {
        log.info("Criando permissão para ação '{}' e sujeito '{}'.", dto.getAction(), dto.getSubject());

        Permission permission = permissionService.create(dto);
        PermissionResponseDTO responseDTO = permissionMapper.toResponseDTO(permission);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Atualiza uma permissão existente",
        description = "Modifica os detalhes de uma permissão específica com base no ID fornecido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Permissão não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> update(
            @Parameter(description = "ID da permissão a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Objeto contendo os novos dados da permissão", required = true)
            @Valid @RequestBody PermissionDTO dto) {
        log.info("Atualizando permissão com ID: {}", id);

        Permission updatedPermission = permissionService.update(id, dto);
        PermissionResponseDTO responseDTO = permissionMapper.toResponseDTO(updatedPermission);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
        summary = "Exclui uma permissão",
        description = "Remove uma permissão do sistema permanentemente com base no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Permissão excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Permissão não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID da permissão a ser excluída", required = true)
            @PathVariable Long id) {
        log.info("Deletando permissão com ID: {}", id);
        permissionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
	    summary = "Associa múltiplas permissões a uma role",
	    description = "Cria vínculos entre uma role e uma lista de permissões."
	)
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Permissões associadas com sucesso"),
	    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
	    @ApiResponse(responseCode = "404", description = "Role ou Permissão não encontrada"),
	    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
	})
	@PostMapping("/associate")
	public ResponseEntity<String> associatePermissionToRole(@Valid @RequestBody RolePermissionsRequest request) {

	    log.info("Associando permissões {} à role ID {}", request.getPermissionIds(), request.getRoleId());
	    permissionService.associatePermissionsToRole(request.getRoleId(), request.getPermissionIds());

	    return ResponseEntity.ok("Permissões associadas à role com sucesso.");
	}

    @Operation(
        summary = "Desassocia múltiplas permissões de uma role",
        description = "Remove a associação de uma lista de permissões de uma role específica."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissões desassociadas da role com sucesso"),
        @ApiResponse(responseCode = "404", description = "Role ou permissão não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping("/disassociate")
    public ResponseEntity<String> disassociatePermissionsFromRole(
            @Valid @RequestBody RolePermissionsRequest request) {

        log.info("Removendo permissões {} da role ID {}", request.getPermissionIds(), request.getRoleId());
        permissionService.disassociatePermissionsFromRole(request.getRoleId(), request.getPermissionIds());

        return ResponseEntity.ok("Permissões desassociadas da role com sucesso.");
    }

}
